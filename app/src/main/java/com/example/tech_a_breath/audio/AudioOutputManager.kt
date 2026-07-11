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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

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
                    if (!_isPlaying.value && intervention is InterventionMode.Masking) {
                        startMasking(
                            intervention.triggerType, 
                            (intervention.level * 100).toInt(),
                            intervention.responseType
                        )
                    }
                } else if (_isPlaying.value) {
                    // Stop if headphones disconnected OR intervention ended
                    stopPlayback()
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
            startMasking(triggerType, maskingLevel, responseType)
        }
    }

    private fun startMasking(triggerType: TriggerType, maskingLevel: Int, responseType: String) {
        val player = player ?: return
        val context = appContext ?: return
        
        // Select audio based on responseType (e.g., "white_noise", "calming_music")
        var resId = context.resources.getIdentifier(responseType, "raw", context.packageName)
        if (resId == 0) {
            println("AudioOutputManager: Warning - resource '$responseType' not found. Falling back to white_noise.")
            resId = context.resources.getIdentifier("white_noise", "raw", context.packageName)
        }
        
        if (resId == 0) {
            println("AudioOutputManager: Error - No masking resources available.")
            return
        }

        val uri = Uri.parse("android.resource://${context.packageName}/$resId")
        val mediaItem = MediaItem.fromUri(uri)
        
        maskingController.setMaskingLevel(maskingLevel)
        maskingController.applyMasking(player)
        
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun stopPlayback() {
        player?.stop()
        releaseAudioFocus()
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
