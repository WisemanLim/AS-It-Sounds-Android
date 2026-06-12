package com.asitsounds.android.core.di

import android.content.Context
import androidx.room.Room
import com.asitsounds.android.core.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()
    @Provides fun provideMediaFileDao(db: AppDatabase): MediaFileDao = db.mediaFileDao()
    @Provides fun provideSegmentDao(db: AppDatabase): SegmentDao = db.segmentDao()
}
