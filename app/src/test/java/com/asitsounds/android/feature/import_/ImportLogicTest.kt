package com.asitsounds.android.feature.import_

import android.content.Intent
import org.junit.Assert.*
import org.junit.Test

class ImportLogicTest {

    // --- isSupportedMime ---

    @Test fun supported_audio() = assertTrue(ImportLogic.isSupportedMime("audio/mpeg"))
    @Test fun supported_video() = assertTrue(ImportLogic.isSupportedMime("video/mp4"))
    @Test fun unsupported_image() = assertFalse(ImportLogic.isSupportedMime("image/png"))
    @Test fun unsupported_null() = assertFalse(ImportLogic.isSupportedMime(null))
    @Test fun unsupported_text() = assertFalse(ImportLogic.isSupportedMime("text/plain"))

    // --- sanitizeFileName ---

    @Test
    fun sanitize_stripsPath() {
        assertEquals("song.mp3", ImportLogic.sanitizeFileName("/storage/emulated/0/song.mp3"))
    }

    @Test
    fun sanitize_stripsBackslashPath() {
        assertEquals("clip.mp4", ImportLogic.sanitizeFileName("C:\\Users\\me\\clip.mp4"))
    }

    @Test
    fun sanitize_replacesIllegalChars() {
        assertEquals("a_b_c.mp3", ImportLogic.sanitizeFileName("a*b?c.mp3"))
    }

    @Test
    fun sanitize_keepsHangul() {
        assertEquals("강의.mp3", ImportLogic.sanitizeFileName("강의.mp3"))
    }

    @Test
    fun sanitize_emptyFallsBack() {
        assertEquals("import", ImportLogic.sanitizeFileName(""))
    }

    // --- uniqueName ---

    @Test
    fun unique_noCollision_unchanged() {
        assertEquals("a.mp3", ImportLogic.uniqueName("a.mp3", setOf("b.mp3")))
    }

    @Test
    fun unique_collision_appendsSuffix() {
        assertEquals("a_1.mp3", ImportLogic.uniqueName("a.mp3", setOf("a.mp3")))
    }

    @Test
    fun unique_multipleCollisions_incrementsSuffix() {
        assertEquals("a_2.mp3", ImportLogic.uniqueName("a.mp3", setOf("a.mp3", "a_1.mp3")))
    }

    @Test
    fun unique_noExtension() {
        assertEquals("file_1", ImportLogic.uniqueName("file", setOf("file")))
    }

    // --- extensionForMime ---

    @Test
    fun ext_mp3() = assertEquals("mp3", ImportLogic.extensionForMime("audio/mpeg"))
    @Test fun ext_m4a() = assertEquals("m4a", ImportLogic.extensionForMime("audio/mp4"))
    @Test fun ext_mp4() = assertEquals("mp4", ImportLogic.extensionForMime("video/mp4"))
    @Test fun ext_unknown() = assertEquals("bin", ImportLogic.extensionForMime("audio/weird"))

    // --- displayNameOrDefault ---

    @Test
    fun displayName_usesProvided() {
        assertEquals("lecture.mp3", ImportLogic.displayNameOrDefault("lecture.mp3", "audio/mpeg"))
    }

    @Test
    fun displayName_nullFallsBackToMimeExt() {
        assertEquals("import.mp3", ImportLogic.displayNameOrDefault(null, "audio/mpeg"))
    }

    @Test
    fun displayName_blankFallsBack() {
        assertEquals("import.mp4", ImportLogic.displayNameOrDefault("  ", "video/mp4"))
    }

    // --- ShareIntake.isShareIntent ---

    @Test
    fun share_send_isShare() = assertTrue(ShareIntake.isShareIntent(Intent.ACTION_SEND))
    @Test fun share_sendMultiple_isShare() = assertTrue(ShareIntake.isShareIntent(Intent.ACTION_SEND_MULTIPLE))
    @Test fun share_main_notShare() = assertFalse(ShareIntake.isShareIntent(Intent.ACTION_MAIN))
    @Test fun share_null_notShare() = assertFalse(ShareIntake.isShareIntent(null))
}
