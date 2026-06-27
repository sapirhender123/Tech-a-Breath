package com.example.tech_a_breath

import com.example.tech_a_breath.ai.TriggerType
import com.example.tech_a_breath.ui.InterventionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TriggerManager {
    private val _activeIntervention = MutableStateFlow<InterventionMode?>(null)
    val activeIntervention: StateFlow<InterventionMode?> = _activeIntervention.asStateFlow()

    fun onTriggerDetected(type: TriggerType) {
        val mode = when (type) {
            TriggerType.SIREN -> InterventionMode.Masking100
            TriggerType.MOTORCYCLE -> InterventionMode.Masking80
            TriggerType.DOG_BARK -> InterventionMode.Masking80
            TriggerType.FIREWORK -> InterventionMode.Masking100
            TriggerType.UNKNOWN -> null
        }
        _activeIntervention.value = mode
    }

    fun stopIntervention() {
        _activeIntervention.value = null
    }
}
