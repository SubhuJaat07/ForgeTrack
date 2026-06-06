package com.forgetrack.app.data.local

import androidx.room.TypeConverter
import com.forgetrack.app.data.model.JobPriority
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.model.PhotoType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStatus(status: JobStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): JobStatus = JobStatus.valueOf(value)

    @TypeConverter
    fun fromPriority(priority: JobPriority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): JobPriority = JobPriority.valueOf(value)

    @TypeConverter
    fun fromPhotoType(type: PhotoType): String = type.name

    @TypeConverter
    fun toPhotoType(value: String): PhotoType = PhotoType.valueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }
}
