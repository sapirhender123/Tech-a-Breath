package com.example.tech_a_breath

import androidx.compose.runtime.mutableStateListOf
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.audio.AudioOutputManager
import com.example.tech_a_breath.data.*
import com.example.tech_a_breath.ui.InterventionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TriggerSettingData(
    val triggerId: Int,
    val configId: Int,
    val type: TriggerType,
    val name: String,
    var maskingLevel: Float = 0.5f,
    var isEnabled: Boolean = true,
    var responseType: String = "white_noise",
    var sensitivityLevel: Int = 3,
    var minMaskingDuration: Int = 3
)

object TriggerManager {
    private val _activeIntervention = MutableStateFlow<InterventionMode?>(null)
    val activeIntervention: StateFlow<InterventionMode?> = _activeIntervention.asStateFlow()

    private val _isAppInForeground = MutableStateFlow(false)
    val isAppInForeground: StateFlow<Boolean> = _isAppInForeground.asStateFlow()

    private val _isProtectionActivated = MutableStateFlow(false)
    val isProtectionActivated: StateFlow<Boolean> = _isProtectionActivated.asStateFlow()

    fun setProtectionActivated(activated: Boolean) {
        _isProtectionActivated.value = activated
    }

    val settings = mutableStateListOf<TriggerSettingData>(
        TriggerSettingData(1, 0, TriggerType.DOG_BARK, "Dog Barking", isEnabled = false),
        TriggerSettingData(2, 0, TriggerType.SIREN, "Ambulance", isEnabled = false),
        TriggerSettingData(3, 0, TriggerType.BABY_CRYING, "Baby Crying", isEnabled = false)
    )

    private val _manualLockTimeLeft = MutableStateFlow(0)
    val manualLockTimeLeft: StateFlow<Int> = _manualLockTimeLeft.asStateFlow()

    private var currentEventId: Long? = null
    private var detectionStartTime: Long = 0
    private var activeMinDuration: Int = 0
    private var isLockedManually: Boolean = false
    private var manualLockUntil: Long = 0

    private var database: AppDatabase? = null
    private var scope: CoroutineScope? = null

    fun init(db: AppDatabase, coroutineScope: CoroutineScope) {
        database = db
        scope = coroutineScope
        loadFromDatabase()
        
        coroutineScope.launch(Dispatchers.Default) {
            while (true) {
                val now = System.currentTimeMillis()
                val timeLeft = if (manualLockUntil > now) ((manualLockUntil - now) / 1000).toInt() else 0
                if (_manualLockTimeLeft.value != timeLeft) {
                    _manualLockTimeLeft.value = timeLeft
                }
                delay(500)
            }
        }
    }

    fun setAppForeground(isInForeground: Boolean) {
        _isAppInForeground.value = isInForeground
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
                            sensitivityLevel = config?.sensitivityLevel ?: 3,
                            minMaskingDuration = config?.minMaskingDuration ?: 3
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
        "baby_crying" -> TriggerType.BABY_CRYING
        else -> TriggerType.UNKNOWN
    }

    private fun mapLabelToDisplayName(label: String): String = when (label) {
        "siren" -> "Ambulance"
        "dog_bark" -> "Dog Barking"
        "baby_crying" -> "Baby Crying"
        else -> label.replaceFirstChar { it.uppercase() }
    }

    fun onTriggerDetected(type: TriggerType) {
        val setting = settings.find { it.type == type }
        if (setting == null || !setting.isEnabled) {
            _activeIntervention.value = null
            return
        }

        isLockedManually = false
        manualLockUntil = 0

        val startTime = System.currentTimeMillis()
        detectionStartTime = startTime
        activeMinDuration = setting.minMaskingDuration

        val mode = InterventionMode.Masking(
            setting.maskingLevel, 
            setting.name, 
            setting.responseType.replace("_", " ").replaceFirstChar { it.uppercase() }, 
            type, 
            setting.responseType
        )
        _activeIntervention.value = mode

        AudioOutputManager.onTriggerDetected(
            type, 
            (setting.maskingLevel * 100).toInt(),
            setting.responseType
        )

        scope?.launch(Dispatchers.IO) {
            val event = TriggerEventEntity(
                eventId = 0,
                triggerId = setting.triggerId,
                detectedAt = startTime,
                maskingPctApplied = (setting.maskingLevel * 100).toInt(),
                responseTypeUsed = setting.responseType,
                latencyMs = System.currentTimeMillis() - startTime,
                eventDurationMs = 0L,
                endedAt = null
            )
            currentEventId = database?.triggerEventDao()?.insert(event)
        }
    }

    fun stopIntervention(force: Boolean = false): Boolean {
        val now = System.currentTimeMillis()
        
        if (isLockedManually && !force && now < manualLockUntil) {
            return false
        }
        
        if (!force) {
            val elapsedMs = now - detectionStartTime
            if (elapsedMs < activeMinDuration * 1000) {
                return false
            }
        }
        
        _activeIntervention.value = null
        isLockedManually = false
        activeMinDuration = 0
        manualLockUntil = 0

        AudioOutputManager.stopPlayback()

        val eventId = currentEventId
        if (eventId != null) {
            scope?.launch(Dispatchers.IO) {
                database?.triggerEventDao()?.getEventById(eventId)?.let { event ->
                    val updatedEvent = event.copy(
                        endedAt = now,
                        eventDurationMs = now - event.detectedAt
                    )
                    database?.triggerEventDao()?.update(updatedEvent)
                }
                currentEventId = null
            }
        }
        return true
    }

    fun setManualLock(locked: Boolean, durationSeconds: Int = 0) {
        isLockedManually = locked
        if (durationSeconds > 0) {
            manualLockUntil = System.currentTimeMillis() + (durationSeconds * 1000)
        } else {
            manualLockUntil = 0
        }
    }

    fun updateSetting(
        triggerId: Int,
        maskingLevel: Float,
        isEnabled: Boolean,
        responseType: String = "white_noise",
        sensitivityLevel: Int = 3,
        minMaskingDuration: Int = 3
    ) {
        val index = settings.indexOfFirst { it.triggerId == triggerId }
        if (index != -1) {
            val updated = settings[index].copy(
                maskingLevel = maskingLevel,
                isEnabled = isEnabled,
                responseType = responseType,
                sensitivityLevel = sensitivityLevel,
                minMaskingDuration = minMaskingDuration
            )
            settings[index] = updated

            val current = _activeIntervention.value
            if (current is InterventionMode.Masking && current.triggerType == updated.type) {
                _activeIntervention.value = current.copy(
                    level = updated.maskingLevel,
                    responseType = updated.responseType
                )
            }

            scope?.launch(Dispatchers.IO) {
                val configEntity = UserTriggerConfigEntity(
                    configId = updated.configId,
                    triggerId = updated.triggerId,
                    isActive = updated.isEnabled,
                    sensitivityLevel = updated.sensitivityLevel,
                    maskingPercentage = (updated.maskingLevel * 100).toInt(),
                    responseType = updated.responseType,
                    minMaskingDuration = updated.minMaskingDuration,
                    updatedAt = System.currentTimeMillis()
                )
                
                val newConfigId = database?.userTriggerConfigDao()?.insertOrUpdateConfig(configEntity)
                
                if (updated.configId == 0 && newConfigId != null) {
                    scope?.launch(Dispatchers.Main) {
                        val currentIndex = settings.indexOfFirst { it.triggerId == triggerId }
                        if (currentIndex != -1) {
                            settings[currentIndex] = settings[currentIndex].copy(configId = newConfigId.toInt())
                        }
                    }
                }

                val history = TriggerConfigHistoryEntity(
                    triggerId = updated.triggerId,
                    isActive = updated.isEnabled,
                    sensitivityLevel = updated.sensitivityLevel,
                    maskingPercentage = (updated.maskingLevel * 100).toInt(),
                    responseType = updated.responseType,
                    minMaskingDuration = updated.minMaskingDuration,
                    changeSource = "user_manual",
                    changedAt = System.currentTimeMillis()
                )
                database?.triggerConfigHistoryDao()?.insert(history)
            }
        }
    }
}
