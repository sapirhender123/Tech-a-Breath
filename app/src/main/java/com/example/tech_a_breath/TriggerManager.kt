package com.example.tech_a_breath

import androidx.compose.runtime.mutableStateListOf
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.data.AppDatabase
import com.example.tech_a_breath.data.TriggerConfigHistoryEntity
import com.example.tech_a_breath.data.TriggerEntity
import com.example.tech_a_breath.data.TriggerEventEntity
import com.example.tech_a_breath.data.UserTriggerConfigEntity
import com.example.tech_a_breath.ui.InterventionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class TriggerSettingData(
    val triggerId: Int,
    val configId: Int,
    val type: TriggerType,
    val name: String,
    var maskingLevel: Float = 0.5f,
    var isEnabled: Boolean = true,
    var responseType: String = "white_noise",
    var sensitivityLevel: Int = 3
)

object TriggerManager {
    private val _activeIntervention = MutableStateFlow<InterventionMode?>(null)
    val activeIntervention: StateFlow<InterventionMode?> = _activeIntervention.asStateFlow()

    // Shared settings state
    val settings = mutableStateListOf<TriggerSettingData>(
        TriggerSettingData(1, 0, TriggerType.MOTORCYCLE, "Motorcycle", isEnabled = false),
        TriggerSettingData(2, 0, TriggerType.DOG_BARK, "Dog Barking", isEnabled = false),
        TriggerSettingData(3, 0, TriggerType.SIREN, "Air Raid Siren", isEnabled = false)
    )

    private var currentEventId: Long? = null
    private var detectionStartTime: Long = 0

    private var database: AppDatabase? = null
    private var scope: CoroutineScope? = null

    fun init(db: AppDatabase, coroutineScope: CoroutineScope) {
        database = db
        scope = coroutineScope
        loadFromDatabase()
    }

    private fun loadFromDatabase() {
        val currentScope = scope ?: return
        currentScope.launch(Dispatchers.IO) {
            val db = database ?: return@launch
            try {
                val triggers = db.triggerDao().getAllTriggers()
                val configs = db.userTriggerConfigDao().getAllConfigs()

                if (triggers.isNotEmpty()) {
                    val loadedSettings = triggers.map { trigger ->
                        val config = configs.find { it.triggerId == trigger.id }
                        TriggerSettingData(
                            triggerId = trigger.id,
                            configId = config?.configId ?: 0,
                            type = mapLabelToType(trigger.modelLabel),
                            name = mapLabelToDisplayName(trigger.modelLabel),
                            maskingLevel = (config?.maskingPercentage ?: 50).toFloat() / 100f,
                            isEnabled = config?.isActive ?: false,
                            responseType = config?.responseType ?: "white_noise",
                            sensitivityLevel = config?.sensitivityLevel ?: 3
                        )
                    }

                    scope?.launch(Dispatchers.Main) {
                settings.clear()
                settings.addAll(loadedSettings)
            }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun mapLabelToType(label: String): TriggerType = when (label) {
        "siren" -> TriggerType.SIREN
        "dog_bark" -> TriggerType.DOG_BARK
        "motorcycle" -> TriggerType.MOTORCYCLE
        "firework" -> TriggerType.FIREWORK
        else -> TriggerType.UNKNOWN
    }

    private fun mapLabelToDisplayName(label: String): String = when (label) {
        "siren" -> "Air Raid Siren"
        "dog_bark" -> "Dog Barking"
        "motorcycle" -> "Motorcycle"
        "firework" -> "Firework"
        else -> label.replaceFirstChar { it.uppercase() }
    }

    fun onTriggerDetected(type: TriggerType) {
        val setting = settings.find { it.type == type }
        
        if (setting == null || !setting.isEnabled) {
            _activeIntervention.value = null
            return
        }

        val startTime = System.currentTimeMillis()
        detectionStartTime = startTime

        val mode = when (setting.responseType) {
            "white_noise" -> InterventionMode.Masking(setting.maskingLevel, "White Noise")
            "music" -> InterventionMode.Masking(setting.maskingLevel, "Calming Music")
            "breathing" -> InterventionMode.Masking(setting.maskingLevel, "Breathing Exercise")
            else -> InterventionMode.Masking(setting.maskingLevel, setting.name)
        }
        _activeIntervention.value = mode

        // Record Event Start
        scope?.launch(Dispatchers.IO) {
            val event = TriggerEventEntity(
                triggerId = setting.triggerId,
                detectedAt = startTime,
                maskingPctApplied = (setting.maskingLevel * 100).toInt(),
                responseTypeUsed = setting.responseType,
                latencyMs = System.currentTimeMillis() - startTime, // Simplified latency
                eventDurationMs = 0,
                endedAt = null
            )
            currentEventId = database?.triggerEventDao()?.insert(event)
        }
    }

    fun stopIntervention() {
        val endTime = System.currentTimeMillis()
        _activeIntervention.value = null

        // Record Event End
        val eventId = currentEventId
        if (eventId != null) {
            scope?.launch(Dispatchers.IO) {
                database?.triggerEventDao()?.getEventById(eventId)?.let { event ->
                    val updatedEvent = event.copy(
                        endedAt = endTime,
                        eventDurationMs = endTime - event.detectedAt
                    )
                    database?.triggerEventDao()?.update(updatedEvent)
                }
                currentEventId = null
            }
        }
    }

    fun updateSetting(
        triggerId: Int,
        maskingLevel: Float,
        isEnabled: Boolean,
        responseType: String = "white_noise",
        sensitivityLevel: Int = 3
    ) {
        val index = settings.indexOfFirst { it.triggerId == triggerId }
        if (index != -1) {
            val updated = settings[index].copy(
                maskingLevel = maskingLevel,
                isEnabled = isEnabled,
                responseType = responseType,
                sensitivityLevel = sensitivityLevel
            )
            settings[index] = updated

            // Save to DB
            scope?.launch(Dispatchers.IO) {
                val timestamp = Instant.now().toString()
                val configEntity = UserTriggerConfigEntity(
                    configId = updated.configId,
                    triggerId = updated.triggerId,
                    isActive = updated.isEnabled,
                    sensitivityLevel = updated.sensitivityLevel,
                    maskingPercentage = (updated.maskingLevel * 100).toInt(),
                    responseType = updated.responseType,
                    updatedAt = timestamp
                )
                
                // insertOrUpdate will handle adding the row if it doesn't exist
                val newConfigId = database?.userTriggerConfigDao()?.insertOrUpdateConfig(configEntity)
                
                // If it was a new insertion (configId was 0), update the memory state with the new PK
                if (updated.configId == 0 && newConfigId != null) {
                    scope?.launch(Dispatchers.Main) {
                        val currentIndex = settings.indexOfFirst { it.triggerId == triggerId }
                        if (currentIndex != -1) {
                            settings[currentIndex] = settings[currentIndex].copy(configId = newConfigId.toInt())
                        }
                    }
                }

                // Record History
                val history = TriggerConfigHistoryEntity(
                    triggerId = updated.triggerId,
                    isActive = updated.isEnabled,
                    sensitivityLevel = updated.sensitivityLevel,
                    maskingPercentage = (updated.maskingLevel * 100).toInt(),
                    responseType = updated.responseType,
                    changedAt = timestamp
                )
                database?.triggerConfigHistoryDao()?.insert(history)
            }
        }
    }
}
