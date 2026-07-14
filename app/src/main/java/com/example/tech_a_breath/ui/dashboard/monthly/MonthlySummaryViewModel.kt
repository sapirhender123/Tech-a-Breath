package com.example.tech_a_breath.ui.dashboard.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech_a_breath.data.*
import com.example.tech_a_breath.data.repository.DashboardRepository
import kotlinx.coroutines.flow.*

// ── UI State ──────────────────────────────────────────────────────────────────

data class MonthlyUiState(
    val isLoading: Boolean = true,
    val countByWeek: List<WeeklyCountDto> = emptyList(),
    val sensitiveDays: List<DayOfWeekCountDto> = emptyList(),
    val hourlyDistribution: List<HourCountDto> = emptyList(),
    val configChanges: List<ConfigChangeDto> = emptyList()
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class MonthlySummaryViewModel(private val repository: DashboardRepository) : ViewModel() {

    val uiState: StateFlow<MonthlyUiState> = combine(
        repository.getMonthlyCountByWeek(),
        repository.getMonthlySensitiveDays(),
        repository.getMonthlyHourlyDistribution(),
        repository.getMonthlyConfigChanges()
    ) { byWeek, sensitiveDays, hourly, changes ->
        MonthlyUiState(
            isLoading           = false,
            countByWeek         = byWeek,
            sensitiveDays       = sensitiveDays,
            hourlyDistribution  = hourly,
            configChanges       = changes
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MonthlyUiState()
        )

    class Factory(private val repository: DashboardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MonthlySummaryViewModel(repository) as T
    }
}
