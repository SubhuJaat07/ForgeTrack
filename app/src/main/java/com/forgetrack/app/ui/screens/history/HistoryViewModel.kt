package com.forgetrack.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Summary statistics derived from the currently filtered job list.
 */
data class SummaryStats(
    val totalRevenue: String = "$0.00",
    val totalHours: String = "0h 0m",
    val totalJobs: String = "0"
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode

    /**
     * All completed and cancelled jobs, used as the base dataset for filtering.
     */
    private val historicalJobs: StateFlow<List<Job>> = jobRepository.getAllJobs()
        .map { jobs -> jobs.filter { it.status == JobStatus.COMPLETED || it.status == JobStatus.CANCELLED } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Jobs filtered by the currently selected [HistoryFilter], sorted by
     * scheduled date descending (most recent first).
     */
    val filteredJobs: StateFlow<List<Job>> = combine(
        historicalJobs,
        _selectedFilter
    ) { jobs, filter ->
        when (filter) {
            HistoryFilter.ALL -> jobs
            HistoryFilter.COMPLETED -> jobs.filter { it.status == JobStatus.COMPLETED }
            HistoryFilter.CANCELLED -> jobs.filter { it.status == JobStatus.CANCELLED }
        }.sortedByDescending { it.scheduledDate }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Aggregate statistics computed from the currently visible filtered jobs.
     */
    val summaryStats: StateFlow<SummaryStats> = filteredJobs
        .map { jobs ->
            val revenue = jobs
                .filter { it.status == JobStatus.COMPLETED }
                .sumOf { it.revenue }
            val totalMinutes = jobs
                .filter { it.status == JobStatus.COMPLETED }
                .sumOf { it.totalDuration }
            val hours = totalMinutes / 3600
            val minutes = (totalMinutes % 3600) / 60

            SummaryStats(
                totalRevenue = "$${"%,.2f".format(revenue)}",
                totalHours = "${hours}h ${minutes}m",
                totalJobs = jobs.size.toString()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SummaryStats()
        )

    /**
     * Changes the active history filter. The [filteredJobs] and
     * [summaryStats] flows react automatically.
     */
    fun onFilterChanged(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    /**
     * Toggles between list and map view modes.
     */
    fun onViewModeChanged(mode: ViewMode) {
        _viewMode.value = mode
    }
}
