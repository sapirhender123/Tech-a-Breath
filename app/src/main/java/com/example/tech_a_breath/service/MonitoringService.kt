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
import androidx.core.app.NotificationCompat
import com.example.tech_a_breath.ai.AudioClassifierManager
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.TriggerManager
import kotlin.concurrent.thread

class MonitoringService : Service() {

    private val CHANNEL_ID = "TechABreathServiceChannel"
    private var isRunning = false
    private var audioRecord: AudioRecord? = null
    private lateinit var classifierManager: AudioClassifierManager

    override fun onCreate() {
        super.onCreate()
        classifierManager = AudioClassifierManager(this)
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
        thread {
            val sampleRate = 16000 // YAMNet expects exactly 16kHz
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                // Initialize TensorFlow Lite Audio Buffer
                val tensorAudio = classifierManager.createInputTensorAudio()

                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED && tensorAudio != null) {
                    audioRecord?.startRecording()
                    println("Tech-a-Breath: AI Audio Monitoring Started")

                    while (isRunning) {
                        // Load latest audio data from microphone into TFLite tensor buffer
                        tensorAudio.load(audioRecord)

                        // Run AI Inference
                        val result = classifierManager.classify(tensorAudio)

                        if (result.triggerType != TriggerType.UNKNOWN) {
                            println("Tech-a-Breath TRIGGER DETECTED: ${result.triggerType} with confidence ${result.confidence}")
                            // Trigger the intervention UI
                            TriggerManager.onTriggerDetected(result.triggerType)
                        } else {
                            // Print fallback just to see the app is alive in Logcat
                            println("Tech-a-Breath: Listening... No trigger found.")
                        }

                        // YAMNet analyzes windows of 0.975 seconds. We sleep slightly to give the buffer time to fill.
                        Thread.sleep(500)
                    }
                }
            } catch (e: SecurityException) {
                println("Tech-a-Breath Error: Microphone permission missing")
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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