package com.asitsounds.android.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun toStatus(value: String): ProjectStatus = ProjectStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(status: ProjectStatus): String = status.name
}

@Database(
    entities = [ProjectEntity::class, MediaFileEntity::class, SegmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun mediaFileDao(): MediaFileDao
    abstract fun segmentDao(): SegmentDao

    companion object {
        const val NAME = "as_it_sounds.db"
    }
}
