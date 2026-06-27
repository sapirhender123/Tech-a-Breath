package com.example.tech_a_breath

import androidx.compose.runtime.mutableStateListOf
import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.ui.InterventionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TriggerSettingData(
    val type: TriggerType,
    val name: String,
    var maskingLevel: Float = 0.5f,
    var isEnabled: Boolean = true
)

object TriggerManager {
    private val _activeIntervention = MutableStateFlow<InterventionMode?>(null)
    val activeIntervention: StateFlow<InterventionMode?> = _activeIntervention.asStateFlow()

    // Shared settings state
    val settings = mutableStateListOf(
        TriggerSettingData(TriggerType.SIREN, "Air Raid Siren", 1.0f),
        TriggerSettingData(TriggerType.DOG_BARK, "Dog Barking", 0.7f),
        TriggerSettingData(TriggerType.MOTORCYCLE, "Motorcycle", 0.8f)
    )

    fun onTriggerDetected(type: TriggerType) {
        val setting = settings.find { it.type == type }
        
        if (setting == null || !setting.isEnabled) {
            _activeIntervention.value = null
            return
        }

        val mode = when (type) {
            TriggerType.SIREN -> InterventionMode.Masking(setting.maskingLevel, "Siren")
            TriggerType.MOTORCYCLE -> InterventionMode.Masking(setting.maskingLevel, "Motorcycle")
            TriggerType.DOG_BARK -> InterventionMode.Masking(setting.maskingLevel, "Dog Barking")
            TriggerType.FIREWORK -> InterventionMode.Masking(setting.maskingLevel, "Firework")
            TriggerType.UNKNOWN -> null
        }
        _activeIntervention.value = mode
    }

    fun stopIntervention() {
        _activeIntervention.value = null
    }

    fun updateSetting(type: TriggerType, maskingLevel: Float, isEnabled: Boolean) {
        val index = settings.indexOfFirst { it.type == type }
        if (index != -1) {
            settings[index] = settings[index].copy(maskingLevel = maskingLevel, isEnabled = isEnabled)
        }
    }
}
