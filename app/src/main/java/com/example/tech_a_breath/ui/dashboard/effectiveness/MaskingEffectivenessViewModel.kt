package com.example.tech_a_breath.ui.dashboard.effectiveness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech_a_breath.data.db.dto.*
import com.example.tech_a_breath.data.repository.DashboardRepository
import kotlinx.coroutines.flow.*

// ── UI State ──────────────────────────────────────────────────────────────────

data class EffectivenessUiState(
    val isLoading: Boolean = true,
    val effectivenessPoints: List<EffectivenessPointDto> = emptyList(),
    val currentMasking: List<CurrentMaskingDto> = emptyList(),
    /** Non-null when we have a Nudge suggestion. */
    val nudge: NudgeSuggestion? = null
)

data class NudgeSuggestion(
    val triggerId: Int,
    val currentPct: Int,
    val suggestedPct: Int,
    val suggestedRating: Double
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class MaskingEffectivenessViewModel(private val repository: DashboardRepository) : ViewModel() {

    val uiState: StateFlow<EffectivenessUiState> = combine(
        repository.getEffectivenessData(),
        repository.getCurrentMasking()
    ) { points, currentMasking ->

        // Compute nudge: for each active trigger, check if a higher masking pct
        // yields a meaningfully better average rating (threshold: +0.5 stars).
        val nudge = currentMasking.firstNotNullOfOrNull { current ->
            val triggerPoints = points.filter { it.triggerId == current.triggerId }
            val currentRating = triggerPoints
                .firstOrNull { it.maskingPctApplied == current.maskingPercentage }
                ?.avgRating ?: return@firstNotNullOfOrNull null

            val best = triggerPoints
                .filter { it.maskingPctApplied > current.maskingPercentage }
                .maxByOrNull { it.avgRating }
                ?: return@firstNotNullOfOrNull null

            if (best.avgRating - currentRating >= 0.5) {
                NudgeSuggestion(
                    triggerId     = current.triggerId,
                    currentPct    = current.maskingPercentage,
                    suggestedPct  = best.maskingPctApplied,
                    suggestedRating = best.avgRating
                )
            } else null
        }

        EffectivenessUiState(
            isLoading            = false,
            effectivenessPoints  = points,
            currentMasking       = currentMasking,
            nudge                = nudge
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EffectivenessUiState()
        )

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaskingEffectivenessViewModel(repository) as T
    }
}
