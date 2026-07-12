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

    fun createInputTensorAudio(): org.tensorflow.lite.support.audio.TensorAudio? {
        val tensor = classifier?.createInputTensorAudio()
        return tensor
    }

    fun classify(audioBuffer: org.tensorflow.lite.support.audio.TensorAudio): TriggerResult {
        val currentClassifier = classifier ?: return TriggerResult(TriggerType.UNKNOWN, 0.0f)

        val results = currentClassifier.classify(audioBuffer)
        
        var bestTriggerType = TriggerType.UNKNOWN
        var maxScore = 0.0f

        for (classification in results) {
            for (category in classification.categories) {
                val label = category.label.lowercase()
                val score = category.score

                if (score > 0.15f && !label.contains("silence") && !label.contains("background") && !label.contains("room") && !label.contains("inside")) {
                    println("Tech-a-Breath AI: Heard '$label' ($score)")
                }

                val currentType = when {
                    label.contains("siren") || label.contains("ambulance") || 
                    label.contains("police") || label.contains("fire engine") || 
                    label.contains("alarm") || label.contains("emergency vehicle") -> TriggerType.SIREN
                    
                    label.contains("barking") || label.contains("howl") || label.contains("dog") ||
                    label.contains("bow-wow") || label.contains("growling") || 
                    label.contains("canine") -> TriggerType.DOG_BARK

                    label.contains("crying") || label.contains("baby") || 
                    label.contains("wail") || label.contains("infant") -> TriggerType.BABY_CRYING
                    
                    else -> null
                }

                val minConfidence = when (currentType) {
                    TriggerType.SIREN -> 0.15f
                    TriggerType.DOG_BARK -> 0.20f
                    TriggerType.BABY_CRYING -> 0.25f
                    else -> 0.25f
                }

                if (currentType != null) {
                    if (score > minConfidence) {
                        val weight = if (score > 0.40f) 2.0f else 1.0f
                        val weightedScore = score * weight
                        
                        if (weightedScore > maxScore) {
                            maxScore = weightedScore
                            bestTriggerType = currentType
                        }
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
