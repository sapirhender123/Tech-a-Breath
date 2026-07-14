package com.example.tech_a_breath.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.tech_a_breath.HeadphoneManager
import com.example.tech_a_breath.TriggerManager
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.ui.InterventionMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Single entry point for all therapeutic audio playback.
 */
object AudioOutputManager {
    private var appContext: Context? = null
    private var player: ExoPlayer? = null
    private var audioManager: AudioManager? = null
    private val maskingController = MaskingController()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var fadeJob: Job? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private var currentResponseType: String? = null

    fun init(context: Context) {
        if (player != null) return
        
        appContext = context.applicationContext
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        player = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
            })
        }

        // Handle connection and intervention state changes
        scope.launch {
            combine(
                HeadphoneManager.isHeadsetConnected,
                TriggerManager.activeIntervention
            ) { connected, intervention ->
                connected to intervention
            }.collectLatest { (connected, intervention) ->
                if (intervention != null && connected) {
                    if (intervention is InterventionMode.Masking) {
                        val targetVolume = (intervention.level * 100).toInt()
                        
                        // Restart if not playing OR if the sound type itself changed
                        if (!_isPlaying.value || currentResponseType != intervention.responseType) {
                            currentResponseType = intervention.responseType
                            startMasking(
                                intervention.triggerType, 
                                targetVolume,
                                intervention.responseType
                            )
                        } else {
                            // Update volume in real-time if only the slider moved
                            maskingController.setMaskingLevel(targetVolume)
                            player?.let { maskingController.applyMasking(it) }
                        }
                    }
                } else if (_isPlaying.value) {
                    // Stop if headphones disconnected OR intervention ended
                    stopPlayback()
                    currentResponseType = null
                }
            }
        }
    }

    fun onTriggerDetected(triggerType: TriggerType, maskingLevel: Int = 100, responseType: String = "white_noise") {
        if (!HeadphoneManager.isHeadsetConnected.value) {
            println("AudioOutputManager: No headphones connected. Aborting playback.")
            return
        }

        if (requestAudioFocus()) {
            currentResponseType = responseType
            startMasking(triggerType, maskingLevel, responseType)
        }
    }

    private fun startMasking(triggerType: TriggerType, maskingLevel: Int, responseType: String) {
        val player = player ?: return
        val context = appContext ?: return
        
        fadeJob?.cancel()
        
        // Select audio based on responseType
        var resId = context.resources.getIdentifier(responseType, "raw", context.packageName)
        if (resId == 0) {
            resId = context.resources.getIdentifier("white_noise", "raw", context.packageName)
        }
        
        if (resId == 0) return

        val uri = Uri.parse("android.resource://${context.packageName}/$resId")
        val mediaItem = MediaItem.fromUri(uri)
        
        maskingController.setMaskingLevel(maskingLevel)
        
        // Start from zero volume for a gentle transition
        player.volume = 0f
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        // Fade in over 1.5 seconds
        fadeJob = scope.launch {
            val targetVolume = maskingLevel / 100f
            val steps = 30
            val delayMs = 50L
            for (i in 1..steps) {
                player.volume = (targetVolume * i) / steps
                delay(delayMs)
            }
        }
    }

    fun stopPlayback() {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val player = player ?: return@launch
            val startVolume = player.volume
            val steps = 20
            for (i in steps downTo 0) {
                player.volume = (startVolume * i) / steps
                delay(30)
            }
            player.stop()
            currentResponseType = null
            releaseAudioFocus()
        }
    }

    private fun requestAudioFocus(): Boolean {
        val audioManager = audioManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build())
                .build()
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // In a full implementation, we'd store the request object to abandon it
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}
