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

        // Search for our specific triggers in the model outputs
        for (classification in results) {
            for (category in classification.categories) {
                val label = category.label.toLowerCase()
                val score = category.score

                if (score > 0.15f) {
                    println("Tech-a-Breath AI: Detected '$label' with score $score")
                }

                // YAMNet official labels mapping
                val triggerType = when {
                    label.contains("siren") -> TriggerType.SIREN
                    label.contains("bark") || label.contains("howl") -> TriggerType.DOG_BARK
                    label.contains("motorcycle") || label.contains("motorbike") -> TriggerType.MOTORCYCLE
                    label.contains("explosion") || label.contains("firework") -> TriggerType.FIREWORK
                    else -> null
                }

                // If a trigger is found with more than 20% confidence, return it immediately
                if (triggerType != null && score > 0.20f) {
                    return TriggerResult(triggerType, score)
                }
            }
        }

        return TriggerResult(TriggerType.UNKNOWN, 0.0f)
    }
}