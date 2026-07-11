package com.example.tech_a_breath.ai

import android.content.Context
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.core.BaseOptions

class AudioClassifierManager(private val context: Context) {

    private var classifier: AudioClassifier? = null

    init {
        initClassifier()
    }

    private fun initClassifier() {
        try {
            val modelName = "yamnet.tflite"
            val options = AudioClassifier.AudioClassifierOptions.builder()
                .setBaseOptions(BaseOptions.builder().setNumThreads(2).build())
                .build()

            classifier = AudioClassifier.createFromFileAndOptions(context, modelName, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createInputTensorAudio() = classifier?.createInputTensorAudio()

    fun classify(audioBuffer: org.tensorflow.lite.support.audio.TensorAudio): TriggerResult {
        val currentClassifier = classifier ?: return TriggerResult(TriggerType.UNKNOWN, 0.0f)

        // Run inference
        val results = currentClassifier.classify(audioBuffer)
        
        var bestTriggerType = TriggerType.UNKNOWN
        var maxScore = 0.0f

        // YAMNet returns multiple classifications, usually sorted by score.
        // We look for the strongest match among our specific PTSD triggers.
        for (classification in results) {
            for (category in classification.categories) {
                val label = category.label.lowercase()
                val score = category.score

                // Log detections above 15% for debugging, excluding broad/unwanted categories
                if (score > 0.15f && !label.contains("silence") && !label.contains("background")) {
                    println("Tech-a-Breath AI: Heard '$label' ($score)")
                }

                // Map specific YAMNet labels to our TriggerTypes
                val currentType = when {
                    label.contains("siren") || label.contains("ambulance") || 
                    label.contains("police") || label.contains("emergency vehicle") ||
                    label.contains("fire engine") || label.contains("alarm") -> TriggerType.SIREN
                    
                    label.contains("barking") || label.contains("howl") || label.contains("dog") ||
                    label.contains("bow-wow") || label.contains("growling") -> TriggerType.DOG_BARK
                    
                    label.contains("motorcycle") || label.contains("motorbike") -> TriggerType.MOTORCYCLE
                    
                    label.contains("explosion") || label.contains("firework") || label.contains("gunshot") -> TriggerType.FIREWORK
                    else -> null
                }

                // SENSITIVITY CALIBRATION:
                // Sirens: Low threshold because they are distinct but waver (0.18f)
                // Dog Barks: Lower threshold because they are very short/transient (0.20f)
                // Others: Standard threshold (0.30f)
                val minConfidence = when (currentType) {
                    TriggerType.SIREN -> 0.18f
                    TriggerType.DOG_BARK -> 0.20f
                    else -> 0.30f
                }

                if (currentType != null) {
                    if (score > minConfidence) {
                        if (score > maxScore) {
                            maxScore = score
                            bestTriggerType = currentType
                        }
                    } else {
                        // Diagnostic: See what sirens are being ignored
                        println("Tech-a-Breath: Ignored '$label' (Score: $score < Min: $minConfidence)")
                    }
                }
            }
        }

        if (bestTriggerType != TriggerType.UNKNOWN) {
            println("Tech-a-Breath !!! VALID TRIGGER: $bestTriggerType ($maxScore)")
            return TriggerResult(bestTriggerType, maxScore)
        }

        return TriggerResult(TriggerType.UNKNOWN, 0.0f)
    }
}