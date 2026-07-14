package com.example.tech_a_breath.ui.dashboard.friendly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech_a_breath.data.prefs.WeeklyRatingStore
import com.example.tech_a_breath.data.repository.DashboardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

data class FriendlyUiState(
    val isLoading: Boolean = true,
    val weeklyCount: Int = 0,
    /** Masking % of the trigger with the most events this week */
    val dominantMaskingPct: Int = 80,
    /** 1–4, null = not yet picked */
    val selectedRating: Int? = null,
    val feedbackSubmitted: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class FriendlyDashboardViewModel(
    private val repository: DashboardRepository,
    private val ratingStore: WeeklyRatingStore
) : ViewModel() {

    private val _selectedRating = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<FriendlyUiState> = combine(
        repository.getWeeklyTotalEvents(),
        repository.getWeeklyCountByTrigger(),
        repository.getCurrentMasking(),
        _selectedRating
    ) { total, byTrigger, currentMasking, rating ->

        // Pick the trigger with the most events this week, look up its current masking %
        val dominantTriggerId = byTrigger.maxByOrNull { it.total }?.triggerId
        val dominantMasking = currentMasking
            .firstOrNull { it.triggerId == dominantTriggerId }
            ?.maskingPercentage ?: 80

        FriendlyUiState(
            isLoading          = false,
            weeklyCount        = total,
            dominantMaskingPct = dominantMasking,
            selectedRating     = rating,
            feedbackSubmitted  = false
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FriendlyUiState()
        )

    fun selectRating(rating: Int) {
        _selectedRating.value = rating
    }

    fun submitFeedback() {
        val rating = _selectedRating.value ?: return
        ratingStore.saveRating(rating)
    }

    /**
     * Applies a ~10% masking reduction to all active triggers.
     * Called when the user taps "Apply" on the Adjustment Suggestion screen.
     */
    fun applyMaskingReduction() {
        viewModelScope.launch {
            repository.getCurrentMasking()
                .first()
                .forEach { current ->
                    val reduced = (current.maskingPercentage * 0.9).toInt().coerceAtLeast(10)
                    repository.updateMaskingPercentage(current.triggerId, reduced)
                }
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val repository: DashboardRepository,
        private val ratingStore: WeeklyRatingStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FriendlyDashboardViewModel(repository, ratingStore) as T
    }
}
