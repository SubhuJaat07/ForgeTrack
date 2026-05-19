package com.forgetrack.app.ui.screens.job

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobPriority
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.repository.ClientRepository
import com.forgetrack.app.data.repository.JobRepository
import com.forgetrack.app.util.generateId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateJobViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    val clients = clientRepository.getAllClients()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _jobCreated = MutableSharedFlow<String>()
    val jobCreated: SharedFlow<String> = _jobCreated

    fun createJob(
        title: String,
        description: String,
        clientId: String,
        clientName: String,
        priority: JobPriority,
        scheduledDate: Long,
        scheduledTime: Long,
        latitude: Double,
        longitude: Double,
        address: String,
        revenue: Double,
        cost: Double,
        tags: String
    ) {
        viewModelScope.launch {
            val job = Job(
                id = generateId(),
                title = title,
                description = description,
                clientId = clientId,
                clientName = clientName,
                status = JobStatus.SCHEDULED,
                priority = priority,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                latitude = latitude,
                longitude = longitude,
                address = address,
                revenue = revenue,
                cost = cost,
                tags = tags
            )
            jobRepository.insertJob(job)
            _jobCreated.emit(job.id)
        }
    }
}
