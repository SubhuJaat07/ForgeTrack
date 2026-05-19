package com.forgetrack.app.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobPriority
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod

    /**
     * Returns the start timestamp (inclusive) for the currently selected period.
     */
    private val periodStartMillis: Flow<Long> = _selectedPeriod.map { period ->
        val cal = Calendar.getInstance()
        when (period) {
            AnalyticsPeriod.WEEK -> cal.add(Calendar.DAY_OF_YEAR, -7)
            AnalyticsPeriod.MONTH -> cal.add(Calendar.DAY_OF_YEAR, -30)
            AnalyticsPeriod.YEAR -> cal.add(Calendar.DAY_OF_YEAR, -365)
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    /**
     * All jobs within the selected time window, used as the base for all analytics.
     */
    private val periodJobs: StateFlow<List<Job>> = periodStartMillis
        .flatMapLatest { start -> jobRepository.getAllJobs() }
        .combine(periodStartMillis) { allJobs, start ->
            allJobs.filter { it.scheduledDate >= start }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Top-level KPI summary for the selected period.
     */
    val kpiData: StateFlow<KpiData> = periodJobs
        .map { jobs ->
            val completedJobs = jobs.filter { it.status == JobStatus.COMPLETED }
            val revenue = completedJobs.sumOf { it.revenue }
            val totalHours = completedJobs.sumOf { it.totalDuration }
            val totalCost = completedJobs.sumOf { it.cost }
            val profit = revenue - totalCost
            val margin = if (revenue > 0) (profit / revenue) * 100 else 0.0

            KpiData(
                revenue = "$${"%,.0f".format(revenue)}",
                jobsCompleted = completedJobs.size.toString(),
                hours = "${totalHours / 3600}h",
                profitMargin = "%.1f%%".format(margin)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = KpiData()
        )

    /**
     * Weekly revenue breakdown as (dayLabel, amount) pairs for the bar chart.
     * Groups completed jobs into the 7 most recent days.
     */
    val weeklyRevenue: StateFlow<List<Pair<String, Float>>> = periodJobs
        .map { jobs ->
            val completedJobs = jobs.filter { it.status == JobStatus.COMPLETED }
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            val cal = Calendar.getInstance()
            val todayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0

            val dailyTotals = Array(7) { 0.0 }

            completedJobs.forEach { job ->
                val jobCal = Calendar.getInstance().apply { timeInMillis = job.scheduledDate }
                val dayIndex = (jobCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                dailyTotals[dayIndex] += job.revenue
            }

            dayNames.indices.map { i ->
                val rotatedIndex = (todayIndex + 1 + i) % 7
                Pair(dayNames[rotatedIndex], dailyTotals[rotatedIndex].toFloat())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Proportion of each [JobStatus] among all period jobs, represented as
     * a fraction (0.0–1.0) suitable for the donut chart.
     */
    val statusDistribution: StateFlow<Map<JobStatus, Float>> = periodJobs
        .map { jobs ->
            if (jobs.isEmpty()) return@map emptyMap()

            val total = jobs.size.toFloat()
            JobStatus.entries.associateWith { status ->
                jobs.count { it.status == status } / total
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    /**
     * Fraction of period jobs that were completed (0.0–1.0).
     */
    val completionRate: StateFlow<Float> = periodJobs
        .map { jobs ->
            if (jobs.isEmpty()) return@map 0f
            jobs.count { it.status == JobStatus.COMPLETED } / jobs.size.toFloat()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    /**
     * Revenue grouped by priority level for the horizontal bar chart.
     */
    val revenueByPriority: StateFlow<List<RevenueByPriorityItem>> = periodJobs
        .map { jobs ->
            val completedJobs = jobs.filter { it.status == JobStatus.COMPLETED }

            JobPriority.entries.map { priority ->
                RevenueByPriorityItem(
                    priority = priority.name,
                    revenue = completedJobs
                        .filter { it.priority == priority }
                        .sumOf { it.revenue }
                        .toFloat()
                )
            }.filter { it.revenue > 0f }
                .sortedByDescending { it.revenue }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Additional performance metrics for the selected period.
     */
    val performanceMetrics: StateFlow<PerformanceMetrics> = periodJobs
        .map { jobs ->
            val completedJobs = jobs.filter { it.status == JobStatus.COMPLETED }

            // Average job value
            val avgJobValue = if (completedJobs.isNotEmpty()) {
                "$${"%,.0f".format(completedJobs.sumOf { it.revenue } / completedJobs.size)}"
            } else "$0"

            // Average duration
            val totalMinutes = completedJobs.sumOf { it.totalDuration }
            val avgMinutes = if (completedJobs.isNotEmpty()) totalMinutes / completedJobs.size else 0
            val avgHours = avgMinutes / 3600
            val avgMins = (avgMinutes % 3600) / 60
            val avgDuration = if (completedJobs.isNotEmpty()) "${avgHours}h ${avgMins}m" else "0h"

            // Best day (day with highest revenue)
            val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val dailyRevenue = Array(7) { 0.0 }
            completedJobs.forEach { job ->
                val cal = Calendar.getInstance().apply { timeInMillis = job.scheduledDate }
                val dayIndex = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                dailyRevenue[dayIndex] += job.revenue
            }
            val bestDayIndex = dailyRevenue.indices.maxByOrNull { dailyRevenue[it] }
            val bestDay = if (bestDayIndex != null && dailyRevenue[bestDayIndex] > 0) {
                dayNames[bestDayIndex]
            } else "N/A"

            // Active (unique) clients
            val activeClients = jobs
                .map { it.clientId }
                .filter { it.isNotBlank() }
                .distinct()
                .size
                .toString()

            PerformanceMetrics(
                avgJobValue = avgJobValue,
                avgDuration = avgDuration,
                bestDay = bestDay,
                activeClients = activeClients
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PerformanceMetrics()
        )

    /**
     * Changes the selected analytics period. All computed flows react
     * automatically by re-filtering jobs within the new time window.
     */
    fun onPeriodChanged(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
    }
}
