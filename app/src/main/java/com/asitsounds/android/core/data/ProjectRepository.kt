package com.asitsounds.android.core.data

import com.asitsounds.android.core.data.db.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val mediaFileDao: MediaFileDao,
    private val segmentDao: SegmentDao
) {
    fun observeProjects(): Flow<List<ProjectEntity>> = projectDao.observeAll()

    fun observeRecent(limit: Int = 3): Flow<List<ProjectEntity>> = projectDao.observeRecent(limit)

    suspend fun getProject(id: Long): ProjectEntity? = projectDao.getById(id)

    suspend fun createProject(name: String, now: Long): Long =
        projectDao.insert(ProjectEntity(name = name, createdAt = now, updatedAt = now))

    suspend fun updateProject(project: ProjectEntity) = projectDao.update(project)

    suspend fun deleteProject(project: ProjectEntity) = projectDao.delete(project)

    fun observeFiles(projectId: Long): Flow<List<MediaFileEntity>> =
        mediaFileDao.observeForProject(projectId)

    suspend fun addFile(file: MediaFileEntity): Long = mediaFileDao.insert(file)

    fun observeSegments(fileId: Long): Flow<List<SegmentEntity>> =
        segmentDao.observeForFile(fileId)

    suspend fun addSegments(segments: List<SegmentEntity>) = segmentDao.insertAll(segments)

    suspend fun lastSegmentIndex(fileId: Long): Int? = segmentDao.lastIndex(fileId)

    suspend fun getFirstFileForProject(projectId: Long): MediaFileEntity? =
        mediaFileDao.getFirstForProject(projectId)

    suspend fun clearSegmentsForFile(fileId: Long) = segmentDao.deleteForFile(fileId)

    suspend fun updateFileStatus(fileId: Long, status: ProjectStatus, error: String? = null) {
        val file = mediaFileDao.getById(fileId) ?: return
        mediaFileDao.update(file.copy(status = status, error = error))
    }

    suspend fun updateProjectStatus(projectId: Long, status: ProjectStatus) {
        val project = projectDao.getById(projectId) ?: return
        projectDao.update(project.copy(status = status, updatedAt = System.currentTimeMillis()))
    }
}
