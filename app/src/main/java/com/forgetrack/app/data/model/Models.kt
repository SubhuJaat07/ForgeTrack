package com.forgetrack.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

enum class JobStatus { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }
enum class JobPriority { LOW, MEDIUM, HIGH, URGENT }
enum class PhotoType { BEFORE, AFTER, PROGRESS }

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val company: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val status: JobStatus = JobStatus.SCHEDULED,
    val priority: JobPriority = JobPriority.MEDIUM,
    val scheduledDate: Long = System.currentTimeMillis(),
    val scheduledTime: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val checkInTime: Long? = null,
    val checkOutTime: Long? = null,
    val timerStart: Long? = null,
    val timerEnd: Long? = null,
    val totalDuration: Long = 0,
    val revenue: Double = 0.0,
    val cost: Double = 0.0,
    val tags: String = "",
    val notes: String = "",
    val voiceNotes: String = "",
    val signaturePath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val profit get() = revenue - cost
    val profitMargin get() = if (revenue > 0) ((profit / revenue) * 100) else 0.0
}

@Entity(tableName = "photos", primaryKeys = ["id", "jobId"])
data class JobPhoto(
    val id: String = "",
    val jobId: String = "",
    val type: PhotoType = PhotoType.BEFORE,
    val uri: String = "",
    val annotation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_notes", primaryKeys = ["id", "jobId"])
data class VoiceNote(
    val id: String = "",
    val jobId: String = "",
    val uri: String = "",
    val transcription: String = "",
    val duration: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class WeeklyStats(
    val totalJobs: Int = 0,
    val completedJobs: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalHours: Long = 0
)
