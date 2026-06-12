package com.asitsounds.android.core.data

import com.asitsounds.android.core.data.db.Converters
import com.asitsounds.android.core.data.db.ProjectStatus
import org.junit.Assert.*
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun status_roundTrips() {
        for (status in ProjectStatus.entries) {
            val stored = converters.fromStatus(status)
            assertEquals(status, converters.toStatus(stored))
        }
    }

    @Test
    fun fromStatus_usesEnumName() {
        assertEquals("PROCESSING", converters.fromStatus(ProjectStatus.PROCESSING))
        assertEquals("DONE", converters.fromStatus(ProjectStatus.DONE))
    }

    @Test
    fun toStatus_parsesName() {
        assertEquals(ProjectStatus.ERROR, converters.toStatus("ERROR"))
        assertEquals(ProjectStatus.PENDING, converters.toStatus("PENDING"))
    }
}
