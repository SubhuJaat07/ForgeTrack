package com.forgetrack.app.data.repository

import com.forgetrack.app.data.local.dao.JobDao
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobPhoto
import com.forgetrack.app.data.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepository @Inject constructor(private val jobDao: JobDao) {

    fun getAllJobs(): Flow<List<Job>> = jobDao.getAllJobs()

    fun getJobById(id: String): Flow<Job?> = jobDao.getJobById(id)

    fun getTodayJobs(startOfDay: Long, endOfDay: Long): Flow<List<Job>> =
        jobDao.getTodayJobs(startOfDay, endOfDay)

    fun getWeekJobs(weekAgo: Long): Flow<List<Job>> = jobDao.getWeekJobs(weekAgo)

    suspend fun insertJob(job: Job) = jobDao.insertJob(job)

    suspend fun updateJob(job: Job) = jobDao.updateJob(job)

    suspend fun deleteJob(id: String) = jobDao.deleteJobById(id)

    suspend fun addPhoto(photo: JobPhoto) = jobDao.insertPhoto(photo)

    fun getPhotosForJob(jobId: String): Flow<List<JobPhoto>> = jobDao.getPhotosForJob(jobId)

    suspend fun deletePhoto(photo: JobPhoto) = jobDao.deletePhoto(photo)

    suspend fun addVoiceNote(note: VoiceNote) = jobDao.insertVoiceNote(note)

    fun getVoiceNotesForJob(jobId: String): Flow<List<VoiceNote>> = jobDao.getVoiceNotesForJob(jobId)

    suspend fun deleteVoiceNote(note: VoiceNote) = jobDao.deleteVoiceNote(note)
}
