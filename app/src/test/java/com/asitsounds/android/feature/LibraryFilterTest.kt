package com.asitsounds.android.feature

import com.asitsounds.android.core.data.db.ProjectEntity
import com.asitsounds.android.core.data.db.ProjectStatus
import com.asitsounds.android.feature.library.LibraryFilter
import com.asitsounds.android.feature.library.StatusFilter
import org.junit.Assert.*
import org.junit.Test

class LibraryFilterTest {

    private fun p(id: Long, name: String, status: ProjectStatus) =
        ProjectEntity(id = id, name = name, createdAt = 0, updatedAt = id, status = status)

    private val projects = listOf(
        p(1, "English Lecture", ProjectStatus.DONE),
        p(2, "French Podcast", ProjectStatus.PROCESSING),
        p(3, "Japanese Interview", ProjectStatus.ERROR),
        p(4, "Spanish Demo", ProjectStatus.PENDING)
    )

    @Test
    fun all_noQuery_returnsEverything() {
        assertEquals(4, LibraryFilter.apply(projects, "", StatusFilter.ALL).size)
    }

    @Test
    fun done_filtersByStatus() {
        val result = LibraryFilter.apply(projects, "", StatusFilter.DONE)
        assertEquals(1, result.size)
        assertEquals("English Lecture", result[0].name)
    }

    @Test
    fun processing_includesPendingAndProcessing() {
        val result = LibraryFilter.apply(projects, "", StatusFilter.PROCESSING)
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "French Podcast" })
        assertTrue(result.any { it.name == "Spanish Demo" })
    }

    @Test
    fun error_filtersError() {
        val result = LibraryFilter.apply(projects, "", StatusFilter.ERROR)
        assertEquals(1, result.size)
        assertEquals("Japanese Interview", result[0].name)
    }

    @Test
    fun query_caseInsensitiveSubstring() {
        val result = LibraryFilter.apply(projects, "podcast", StatusFilter.ALL)
        assertEquals(1, result.size)
        assertEquals("French Podcast", result[0].name)
    }

    @Test
    fun query_trimmed() {
        val result = LibraryFilter.apply(projects, "  english  ", StatusFilter.ALL)
        assertEquals(1, result.size)
    }

    @Test
    fun query_andStatus_combined() {
        // "demo" matches Spanish Demo (PENDING). PROCESSING filter includes PENDING.
        val result = LibraryFilter.apply(projects, "demo", StatusFilter.PROCESSING)
        assertEquals(1, result.size)
        assertEquals("Spanish Demo", result[0].name)
    }

    @Test
    fun query_noMatch_returnsEmpty() {
        assertTrue(LibraryFilter.apply(projects, "zzz", StatusFilter.ALL).isEmpty())
    }

    @Test
    fun statusFilter_matchesLogic() {
        assertTrue(StatusFilter.ALL.matches(ProjectStatus.ERROR))
        assertTrue(StatusFilter.DONE.matches(ProjectStatus.DONE))
        assertFalse(StatusFilter.DONE.matches(ProjectStatus.ERROR))
        assertTrue(StatusFilter.PROCESSING.matches(ProjectStatus.PENDING))
    }
}
