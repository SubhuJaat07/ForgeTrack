package com.forgetrack.app.ui.screens.job

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeFilter = MutableStateFlow("all")
    val activeFilter: StateFlow<String> = _activeFilter

    private val _sortBy = MutableStateFlow("date")
    val sortBy: StateFlow<String> = _sortBy

    val allJobs: StateFlow<List<Job>> = jobRepository.getAllJobs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredJobs: StateFlow<List<Job>> = combine(
        allJobs, searchQuery, activeFilter, sortBy
    ) { jobs, query, filter, sort ->
        var result = jobs

        if (filter != "all") {
            result = result.filter { it.status.name == filter }
        }

        if (query.isNotBlank()) {
            val q = query.lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.clientName.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }

        result = when (sort) {
            "priority" -> result.sortedBy { listOf("URGENT","HIGH","MEDIUM","LOW").indexOf(it.priority.name) }
            "revenue" -> result.sortedByDescending { it.revenue }
            else -> result.sortedByDescending { it.scheduledDate }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearch(query: String) { _searchQuery.value = query }
    fun updateFilter(filter: String) { _activeFilter.value = filter }
    fun toggleSort() {
        _sortBy.value = when (_sortBy.value) {
            "date" -> "priority"
            "priority" -> "revenue"
            else -> "date"
        }
    }

    fun deleteJob(id: String) {
        viewModelScope.launch { jobRepository.deleteJob(id) }
    }
}
