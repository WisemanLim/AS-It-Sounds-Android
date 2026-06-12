package com.asitsounds.android.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert suspend fun insert(project: ProjectEntity): Long
    @Update suspend fun update(project: ProjectEntity)
    @Delete suspend fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ProjectEntity>>
}

@Dao
interface MediaFileDao {
    @Insert suspend fun insert(file: MediaFileEntity): Long
    @Update suspend fun update(file: MediaFileEntity)

    @Query("SELECT * FROM media_files WHERE projectId = :projectId")
    fun observeForProject(projectId: Long): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getById(id: Long): MediaFileEntity?
}

@Dao
interface SegmentDao {
    @Insert suspend fun insert(segment: SegmentEntity): Long
    @Insert suspend fun insertAll(segments: List<SegmentEntity>)

    @Query("SELECT * FROM segments WHERE fileId = :fileId ORDER BY idx ASC")
    fun observeForFile(fileId: Long): Flow<List<SegmentEntity>>

    @Query("SELECT * FROM segments WHERE fileId = :fileId ORDER BY idx ASC")
    suspend fun getForFile(fileId: Long): List<SegmentEntity>

    @Query("SELECT MAX(idx) FROM segments WHERE fileId = :fileId")
    suspend fun lastIndex(fileId: Long): Int?
}
