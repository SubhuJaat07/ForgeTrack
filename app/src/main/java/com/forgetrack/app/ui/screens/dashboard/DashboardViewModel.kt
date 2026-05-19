package com.forgetrack.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.local.UserPreferences
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.model.WeeklyStats
import com.forgetrack.app.data.repository.JobRepository
import com.forgetrack.app.util.todayEnd
import com.forgetrack.app.util.todayStart
import com.forgetrack.app.util.weekAgo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isOnboarded = userPreferences.isOnboarded

    private val _todayJobs = MutableStateFlow<List<Job>>(emptyList())
    val todayJobs: StateFlow<List<Job>> = _todayJobs

    private val _weeklyStats = MutableStateFlow(WeeklyStats())
    val weeklyStats: StateFlow<WeeklyStats> = _weeklyStats

    init {
        viewModelScope.launch {
            jobRepository.getTodayJobs(todayStart(), todayEnd()).collect { jobs ->
                _todayJobs.value = jobs
            }
        }
        viewModelScope.launch {
            jobRepository.getWeekJobs(weekAgo()).collect { jobs ->
                _weeklyStats.value = WeeklyStats(
                    totalJobs = jobs.size,
                    completedJobs = jobs.count { it.status == JobStatus.COMPLETED },
                    totalRevenue = jobs.filter { it.status == JobStatus.COMPLETED }.sumOf { it.revenue },
                    totalHours = jobs.filter { it.status == JobStatus.COMPLETED }.sumOf { it.totalDuration }
                )
            }
        }
    }

    fun setOnboarded() {
        viewModelScope.launch { userPreferences.setOnboarded(true) }
    }
}
