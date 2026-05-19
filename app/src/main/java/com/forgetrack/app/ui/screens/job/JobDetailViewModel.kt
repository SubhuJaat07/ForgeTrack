package com.forgetrack.app.ui.screens.job

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobPhoto
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.model.PhotoType
import com.forgetrack.app.data.model.VoiceNote
import com.forgetrack.app.data.repository.JobRepository
import com.forgetrack.app.util.generateId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job as CoroutineJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _job = MutableStateFlow<Job?>(null)
    val job: StateFlow<Job?> = _job

    private val _photos = MutableStateFlow<List<JobPhoto>>(emptyList())
    val photos: StateFlow<List<JobPhoto>> = _photos

    private val _voiceNotes = MutableStateFlow<List<VoiceNote>>(emptyList())
    val voiceNotes: StateFlow<List<VoiceNote>> = _voiceNotes

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private var timerJob: CoroutineJob? = null

    private val _notes = MutableStateFlow<List<String>>(emptyList())
    val notes: StateFlow<List<String>> = _notes

    private val _showSignature = MutableStateFlow(false)
    val showSignature: StateFlow<Boolean> = _showSignature

    private val _signatureSaved = MutableStateFlow(false)
    val signatureSaved: StateFlow<Boolean> = _signatureSaved

    fun loadJob(jobId: String) {
        viewModelScope.launch {
            jobRepository.getJobById(jobId).collect { job ->
                _job.value = job
                if (job != null) {
                    _notes.value = if (job.notes.isBlank()) emptyList() else job.notes.split("|||")
                }
            }
        }
        viewModelScope.launch {
            jobRepository.getPhotosForJob(jobId).collect { _photos.value = it }
        }
        viewModelScope.launch {
            jobRepository.getVoiceNotesForJob(jobId).collect { _voiceNotes.value = it }
        }
    }

    fun startTimer() {
        val currentJob = _job.value ?: return
        val startTime = System.currentTimeMillis()
        _isTimerRunning.value = true
        _elapsedSeconds.value = 0
        timerJob = viewModelScope.launch {
            val startTotal = if (currentJob.totalDuration > 0) currentJob.totalDuration else 0
            while (isActive) {
                delay(1000)
                _elapsedSeconds.value = startTotal + ((System.currentTimeMillis() - startTime) / 1000)
            }
        }
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        val total = _elapsedSeconds.value
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.updateJob(currentJob.copy(
                status = JobStatus.COMPLETED,
                timerEnd = System.currentTimeMillis(),
                totalDuration = total,
                completedAt = System.currentTimeMillis()
            ))
        }
    }

    fun startJob() {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.updateJob(currentJob.copy(
                status = JobStatus.IN_PROGRESS,
                timerStart = System.currentTimeMillis()
            ))
        }
        startTimer()
    }

    fun cancelJob() {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.updateJob(currentJob.copy(status = JobStatus.CANCELLED))
        }
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun checkIn(latitude: Double, longitude: Double, address: String) {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.updateJob(currentJob.copy(
                latitude = latitude,
                longitude = longitude,
                address = address,
                checkInTime = System.currentTimeMillis()
            ))
        }
    }

    fun checkOut() {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.updateJob(currentJob.copy(
                checkOutTime = System.currentTimeMillis()
            ))
        }
    }

    fun addPhoto(uri: String, type: PhotoType) {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.addPhoto(JobPhoto(
                id = generateId(),
                jobId = currentJob.id,
                type = type,
                uri = uri
            ))
        }
    }

    fun addVoiceNote(uri: String, transcription: String, duration: Long) {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.addVoiceNote(VoiceNote(
                id = generateId(),
                jobId = currentJob.id,
                uri = uri,
                transcription = transcription,
                duration = duration
            ))
        }
    }

    fun addNote(text: String) {
        val currentJob = _job.value ?: return
        val updatedNotes = _notes.value + text
        _notes.value = updatedNotes
        viewModelScope.launch {
            jobRepository.updateJob(currentJob.copy(notes = updatedNotes.joinToString("|||")))
        }
    }

    fun saveSignature(path: String) {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.updateJob(currentJob.copy(signaturePath = path))
            _signatureSaved.value = true
            _showSignature.value = false
        }
    }

    fun toggleSignatureDialog() { _showSignature.value = !_showSignature.value }

    fun deleteJob() {
        val currentJob = _job.value ?: return
        viewModelScope.launch {
            jobRepository.deleteJob(currentJob.id)
        }
    }
}
