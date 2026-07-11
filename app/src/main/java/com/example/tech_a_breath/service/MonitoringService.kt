package com.example.tech_a_breath.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.tech_a_breath.MainActivity
import com.example.tech_a_breath.ai.AudioClassifierManager
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.TriggerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MonitoringService : Service() {

    private val CHANNEL_ID = "TechABreathServiceChannel"
    private val NOTIFICATION_ID = 1
    private val ACTION_STOP_MASKING = "com.example.tech_a_breath.STOP_MASKING"
    private val ACTION_EXTEND_1M = "com.example.tech_a_breath.EXTEND_1M"
    private val ACTION_EXTEND_3M = "com.example.tech_a_breath.EXTEND_3M"
    private val ACTION_EXTEND_5M = "com.example.tech_a_breath.EXTEND_5M"
    
    private var isRunning = false
    private var audioRecord: AudioRecord? = null
    private lateinit var classifierManager: AudioClassifierManager
    private val detectionHistory = mutableListOf<TriggerType>()
    private val HISTORY_SIZE = 5 
    private var volumeThresholdDb = -50.0 
    private var lastActiveTrigger: TriggerType = TriggerType.UNKNOWN
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            combine(
                TriggerManager.activeIntervention,
                TriggerManager.isAppInForeground
            ) { intervention, isForeground ->
                Pair(intervention != null, isForeground)
            }.collectLatest { state ->
                updateNotificationState(state.first, state.second)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_MASKING -> TriggerManager.stopIntervention(force = true)
            ACTION_EXTEND_1M -> TriggerManager.setManualLock(true, 1)
            ACTION_EXTEND_3M -> TriggerManager.setManualLock(true, 3)
            ACTION_EXTEND_5M -> TriggerManager.setManualLock(true, 5)
        }

        createNotificationChannel()
        // Initial notification to start foreground
        val notification = createNotification(false, false)
        startForeground(NOTIFICATION_ID, notification)

        if (!isRunning) {
            isRunning = true
            startMonitoring()
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        thread(name = "AIThread") {
            try {
                classifierManager = AudioClassifierManager(this@MonitoringService)
                
                val sampleRate = 16000
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

                if (androidx.core.content.ContextCompat.checkSelfPermission(this@MonitoringService, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
                } else {
                    return@thread
                }

                val tensorAudio = classifierManager.createInputTensorAudio()

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED && tensorAudio != null) {
                    audioRecord?.startRecording()
                    
                    while (isRunning) {
                        val shortBuffer = ShortArray(1600) // 100ms
                        val readCount = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: 0
                        
                        if (readCount > 0) {
                            val dbLevel = calculateDb(shortBuffer, readCount)
                            tensorAudio.load(shortBuffer, 0, readCount)

                            val currentTrigger = if (dbLevel > volumeThresholdDb) {
                                classifierManager.classify(tensorAudio).triggerType
                            } else {
                                TriggerType.UNKNOWN
                            }

                            detectionHistory.add(currentTrigger)
                            if (detectionHistory.size > HISTORY_SIZE) detectionHistory.removeAt(0)

                            val consistentTrigger = getConsistentTrigger()
                            if (consistentTrigger != lastActiveTrigger) {
                                if (consistentTrigger != TriggerType.UNKNOWN) {
                                    println("🚨🚨 [Tech-a-Breath System] TRIGGER CONFIRMED: $consistentTrigger")
                                    TriggerManager.onTriggerDetected(consistentTrigger)
                                    lastActiveTrigger = consistentTrigger
                                } else if (lastActiveTrigger != TriggerType.UNKNOWN) {
                                    if (TriggerManager.stopIntervention()) {
                                        println("✅ [Tech-a-Breath System] Environment cleared.")
                                        lastActiveTrigger = consistentTrigger
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateDb(buffer: ShortArray, readCount: Int): Double {
        var sum = 0.0
        for (i in 0 until readCount) {
            sum += buffer[i].toDouble() * buffer[i].toDouble()
        }
        val rms = Math.sqrt(sum / readCount)
        return 20 * Math.log10(rms / 32768.0)
    }

    private fun getConsistentTrigger(): TriggerType {
        if (detectionHistory.isEmpty()) return TriggerType.UNKNOWN
        
        val targetCounts = detectionHistory.filter { it != TriggerType.UNKNOWN }
            .groupingBy { it }
            .eachCount()

        for ((trigger, count) in targetCounts) {
            when (trigger) {
                TriggerType.DOG_BARK -> {
                    if (count >= 1) return trigger
                }
                TriggerType.SIREN, TriggerType.BABY_CRYING -> {
                    if (count >= 2) return trigger
                }
                else -> {}
            }
        }
            
        return TriggerType.UNKNOWN
    }

    private fun updateNotificationState(isInterventionActive: Boolean, isForeground: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = createNotification(isInterventionActive, isForeground)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(isInterventionActive: Boolean, silent: Boolean = false): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelToUse = if (isInterventionActive && !silent) "InterventionChannel" else CHANNEL_ID
        val builder = NotificationCompat.Builder(this, channelToUse)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        if (silent) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle("Acoustic Shield Active")
                .setContentText("Monitoring in background")
        } else if (isInterventionActive) {
            val stopIntent = Intent(this, MonitoringService::class.java).apply { action = ACTION_STOP_MASKING }
            val stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val ext1Intent = Intent(this, MonitoringService::class.java).apply { action = ACTION_EXTEND_1M }
            val ext1PI = PendingIntent.getService(this, 2, ext1Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val ext3Intent = Intent(this, MonitoringService::class.java).apply { action = ACTION_EXTEND_3M }
            val ext3PI = PendingIntent.getService(this, 3, ext3Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val ext5Intent = Intent(this, MonitoringService::class.java).apply { action = ACTION_EXTEND_5M }
            val ext5PI = PendingIntent.getService(this, 4, ext5Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            builder.setContentTitle("Protection Active")
                .setContentText("Acoustic Shield is currently masking a trigger.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Safe Now", stopPendingIntent)
                .addAction(android.R.drawable.ic_menu_recent_history, "Just a moment", ext1PI)
                .addAction(android.R.drawable.ic_menu_recent_history, "Take a breath", ext3PI)
                .addAction(android.R.drawable.ic_menu_recent_history, "Stay with me", ext5PI)
                .setFullScreenIntent(pendingIntent, true)
        } else {
            builder.setContentTitle("Shield Active")
                .setContentText("Listening to environment...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Tech-a-Breath Monitoring Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when the app is listening for sounds"
                setShowBadge(false)
            }
            manager.createNotificationChannel(serviceChannel)
            
            val interventionChannel = NotificationChannel(
                "InterventionChannel",
                "Shield Intervention",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows when masking is active"
                enableVibration(false)
                vibrationPattern = longArrayOf(0)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(interventionChannel)
        }
    }
}
