package com.asitsounds.android.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asitsounds.android.core.data.db.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var projectDao: ProjectDao
    private lateinit var fileDao: MediaFileDao
    private lateinit var segmentDao: SegmentDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        projectDao = db.projectDao()
        fileDao = db.mediaFileDao()
        segmentDao = db.segmentDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun insertProject_retrievable() = runTest {
        val id = projectDao.insert(ProjectEntity(name = "Test", createdAt = 1, updatedAt = 1))
        val loaded = projectDao.getById(id)
        assertNotNull(loaded)
        assertEquals("Test", loaded!!.name)
    }

    @Test
    fun observeRecent_ordersByUpdatedDesc() = runTest {
        projectDao.insert(ProjectEntity(name = "Old", createdAt = 1, updatedAt = 10))
        projectDao.insert(ProjectEntity(name = "New", createdAt = 2, updatedAt = 20))
        val recent = projectDao.observeRecent(3).first()
        assertEquals("New", recent[0].name)
    }

    @Test
    fun statusConverter_persists() = runTest {
        val id = projectDao.insert(
            ProjectEntity(name = "S", createdAt = 1, updatedAt = 1, status = ProjectStatus.DONE)
        )
        assertEquals(ProjectStatus.DONE, projectDao.getById(id)!!.status)
    }

    @Test
    fun cascadeDelete_removesFilesAndSegments() = runTest {
        val pid = projectDao.insert(ProjectEntity(name = "P", createdAt = 1, updatedAt = 1))
        val fid = fileDao.insert(
            MediaFileEntity(projectId = pid, path = "/x", displayName = "x.mp3",
                mime = "audio/mpeg", durationMs = 1000, createdAt = 1)
        )
        segmentDao.insertAll(
            listOf(
                SegmentEntity(fileId = fid, idx = 0, startMs = 0, endMs = 500,
                    textEn = "Hi", textHangul = "하이", textKo = "안녕"),
                SegmentEntity(fileId = fid, idx = 1, startMs = 500, endMs = 1000,
                    textEn = "Bye", textHangul = "바이", textKo = "잘가")
            )
        )
        assertEquals(2, segmentDao.getForFile(fid).size)

        // Deleting the project cascades to files → segments
        projectDao.delete(projectDao.getById(pid)!!)
        assertNull(fileDao.getById(fid))
        assertEquals(0, segmentDao.getForFile(fid).size)
    }

    @Test
    fun lastIndex_returnsMaxSegmentIndex() = runTest {
        val pid = projectDao.insert(ProjectEntity(name = "P", createdAt = 1, updatedAt = 1))
        val fid = fileDao.insert(
            MediaFileEntity(projectId = pid, path = "/x", displayName = "x", mime = "audio/mpeg",
                durationMs = 1, createdAt = 1)
        )
        segmentDao.insertAll(
            listOf(
                SegmentEntity(fileId = fid, idx = 0, startMs = 0, endMs = 1, textEn = "a", textHangul = "ㅏ", textKo = "ㄱ"),
                SegmentEntity(fileId = fid, idx = 1, startMs = 1, endMs = 2, textEn = "b", textHangul = "ㅑ", textKo = "ㄴ"),
                SegmentEntity(fileId = fid, idx = 2, startMs = 2, endMs = 3, textEn = "c", textHangul = "ㅓ", textKo = "ㄷ")
            )
        )
        assertEquals(2, segmentDao.lastIndex(fid))
    }
}
