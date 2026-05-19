package com.forgetrack.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.forgetrack.app.data.local.dao.ClientDao
import com.forgetrack.app.data.local.dao.JobDao
import com.forgetrack.app.data.model.Client
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobPhoto
import com.forgetrack.app.data.model.VoiceNote
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [Job::class, Client::class, JobPhoto::class, VoiceNote::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun clientDao(): ClientDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "forgetrack_db"
        ).addMigrations()
         .fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideJobDao(db: AppDatabase): JobDao = db.jobDao()

    @Provides
    fun provideClientDao(db: AppDatabase): ClientDao = db.clientDao()
}
