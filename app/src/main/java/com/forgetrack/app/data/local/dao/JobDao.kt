package com.forgetrack.app.data.local.dao

import androidx.room.*
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobPhoto
import com.forgetrack.app.data.model.VoiceNote
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY scheduledDate DESC")
    fun getAllJobs(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun getJobById(id: String): Flow<Job?>

    @Query("SELECT * FROM jobs WHERE status = :status ORDER BY scheduledDate DESC")
    fun getJobsByStatus(status: String): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE scheduledDate >= :startOfDay AND scheduledDate < :endOfDay AND status != 'CANCELLED' ORDER BY scheduledDate ASC")
    fun getTodayJobs(startOfDay: Long, endOfDay: Long): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE scheduledDate >= :weekAgo AND status != 'CANCELLED' ORDER BY scheduledDate DESC")
    fun getWeekJobs(weekAgo: Long): Flow<List<Job>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job)

    @Update
    suspend fun updateJob(job: Job)

    @Delete
    suspend fun deleteJob(job: Job)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJobById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: JobPhoto)

    @Query("SELECT * FROM photos WHERE jobId = :jobId ORDER BY timestamp ASC")
    fun getPhotosForJob(jobId: String): Flow<List<JobPhoto>>

    @Delete
    suspend fun deletePhoto(photo: JobPhoto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(note: VoiceNote)

    @Query("SELECT * FROM voice_notes WHERE jobId = :jobId ORDER BY timestamp ASC")
    fun getVoiceNotesForJob(jobId: String): Flow<List<VoiceNote>>

    @Delete
    suspend fun deleteVoiceNote(note: VoiceNote)
}
