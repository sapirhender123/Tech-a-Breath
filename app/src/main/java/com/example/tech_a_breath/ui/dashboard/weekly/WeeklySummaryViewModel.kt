package com.example.tech_a_breath.ui.dashboard.weekly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech_a_breath.data.db.dto.*
import com.example.tech_a_breath.data.repository.DashboardRepository
import kotlinx.coroutines.flow.*

// ── UI State ──────────────────────────────────────────────────────────────────

data class WeeklyUiState(
    val isLoading: Boolean = true,
    val totalEvents: Int = 0,
    val countByTrigger: List<TriggerCountDto> = emptyList(),
    val countByDay: List<DayOfWeekCountDto> = emptyList(),
    val avgDurationByTrigger: List<TriggerAvgDurationDto> = emptyList(),
    val durationBreakdown: List<DurationBreakdownDto> = emptyList()
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class WeeklySummaryViewModel(private val repository: DashboardRepository) : ViewModel() {

    val uiState: StateFlow<WeeklyUiState> = combine(
        repository.getWeeklyTotalEvents(),
        repository.getWeeklyCountByTrigger(),
        repository.getWeeklyCountByDayOfWeek(),
        repository.getWeeklyAvgDurationByTrigger(),
        repository.getWeeklyDurationBreakdown()
    ) { total, byTrigger, byDay, avgDuration, breakdown ->
        WeeklyUiState(
            isLoading            = false,
            totalEvents          = total,
            countByTrigger       = byTrigger,
            countByDay           = byDay,
            avgDurationByTrigger = avgDuration,
            durationBreakdown    = breakdown
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeeklyUiState()
        )

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WeeklySummaryViewModel(repository) as T
    }
}
