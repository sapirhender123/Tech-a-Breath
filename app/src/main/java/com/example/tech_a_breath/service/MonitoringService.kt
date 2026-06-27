package com.example.tech_a_breath.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import java.util.Locale
import androidx.core.app.NotificationCompat
import com.example.tech_a_breath.ai.AudioClassifierManager
import com.example.tech_a_breath.ai.TriggerType
import kotlin.concurrent.thread

class MonitoringService : Service() {

    private val CHANNEL_ID = "TechABreathServiceChannel"
    private var isRunning = false
    private var audioRecord: AudioRecord? = null
    private lateinit var classifierManager: AudioClassifierManager
    private val detectionHistory = mutableListOf<TriggerType>()
    private val HISTORY_SIZE = 3
    private var volumeThresholdDb = -50.0 // More sensitive
    private var lastActiveTrigger: TriggerType = TriggerType.UNKNOWN

    override fun onCreate() {
        super.onCreate()
        // Removed blocking init
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)

        if (!isRunning) {
            isRunning = true
            startMonitoring()
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        thread(name = "AIThread") {
            try {
                // Initialize the AI model on background thread
                classifierManager = AudioClassifierManager(this@MonitoringService)
                
                val sampleRate = 16000
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

                if (androidx.core.content.ContextCompat.checkSelfPermission(this@MonitoringService, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        bufferSize
                    )
                } else {
                    println("Tech-a-Breath Error: Microphone permission missing")
                    return@thread
                }

                val tensorAudio = classifierManager.createInputTensorAudio()
                val audioBuffer = ShortArray(bufferSize)

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED && tensorAudio != null) {
                    audioRecord?.startRecording()
                    println("Tech-a-Breath: AI Real-Time Monitoring Started")

                    while (isRunning) {
                        // Read in smaller chunks to avoid missing fast sounds like barks
                        val shortBuffer = ShortArray(3200) // 200ms chunk
                        val readCount = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: 0
                        
                        if (readCount > 0) {
                            // Calculate volume for Stage 1
                            val dbLevel = calculateDb(shortBuffer, readCount)
                            
                            // Load chunk into the AI's internal ring buffer
                            tensorAudio.load(shortBuffer, 0, readCount)

                            if (dbLevel > volumeThresholdDb) {
                                // Run inference on the 1-second sliding window
                                val result = classifierManager.classify(tensorAudio)

                                if (result.triggerType != TriggerType.UNKNOWN) {
                                    detectionHistory.add(result.triggerType)
                                    if (detectionHistory.size > HISTORY_SIZE) detectionHistory.removeAt(0)

                                    val consistentTrigger = getConsistentTrigger()
                                    if (consistentTrigger != lastActiveTrigger) {
                                        if (consistentTrigger != TriggerType.UNKNOWN) {
                                            println("🚨🚨 [Tech-a-Breath System] TRIGGER CONFIRMED: $consistentTrigger")
                                        } else if (lastActiveTrigger != TriggerType.UNKNOWN) {
                                            println("✅ [Tech-a-Breath System] Environment cleared.")
                                        }
                                        lastActiveTrigger = consistentTrigger
                                    }
                                }
                            } else {
                                // Occasionally log ambient
                                if (System.currentTimeMillis() % 10000 < 200) {
                                    println("Tech-a-Breath: Monitoring ambient (${String.format(Locale.US, "%.1f", dbLevel)} dB)")
                                }
                            }
                        }
                        // Very short sleep to prevent CPU pegging but keep sampling fast
                        Thread.sleep(100)
                    }
                }
            } catch (e: Exception) {
                println("Tech-a-Breath Thread Error: ${e.message}")
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
        // Convert to dB relative to full scale (32768.0 for 16-bit PCM)
        return 20 * Math.log10(rms / 32768.0)
    }

    private fun getConsistentTrigger(): TriggerType {
        if (detectionHistory.isEmpty()) return TriggerType.UNKNOWN
        
        // Count occurrences of specific target triggers
        val targetCounts = detectionHistory.filter { it != TriggerType.UNKNOWN }
            .groupingBy { it }
            .eachCount()

        // PERSISTENCE STRATEGY:
        // 1. For "Continuous" triggers (Siren, Motorcycle), require 2/3 windows to prevent blips.
        // 2. For "Impact" triggers (Dog Bark, Firework), a single detection is enough (1/3 windows).
        
        for ((trigger, count) in targetCounts) {
            when (trigger) {
                TriggerType.DOG_BARK, TriggerType.FIREWORK -> {
                    if (count >= 1) return trigger
                }
                TriggerType.SIREN, TriggerType.MOTORCYCLE -> {
                    if (count >= 2) return trigger
                }
                else -> {}
            }
        }
            
        return TriggerType.UNKNOWN
    }


    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shield Active")
            .setContentText("Listening to environment...")
            .setSmallIcon(android.R.drawable.ic_lock_lock) // Temporary built-in system icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Tech-a-Breath Monitoring Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            // Correct way to get the NotificationManager in Kotlin
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }
}