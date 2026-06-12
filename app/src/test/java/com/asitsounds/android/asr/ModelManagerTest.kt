package com.asitsounds.android.asr

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.security.MessageDigest

class ModelManagerTest {

    @get:Rule
    val tmp = TemporaryFolder()

    // sha256 helper is pure file I/O — no Android context needed

    @Test
    fun sha256_knownContent_matchesExpected() {
        val file = tmp.newFile("test.bin")
        file.writeBytes("hello whisper".toByteArray())
        val expected = sha256Hex("hello whisper".toByteArray())
        assertEquals(expected, ModelManager.sha256(file))
    }

    @Test
    fun sha256_emptyFile_matchesExpected() {
        val file = tmp.newFile("empty.bin")
        val expected = sha256Hex(ByteArray(0))
        assertEquals(expected, ModelManager.sha256(file))
    }

    @Test
    fun sha256_differentContents_differentHashes() {
        val f1 = tmp.newFile("a.bin").also { it.writeBytes("aaa".toByteArray()) }
        val f2 = tmp.newFile("b.bin").also { it.writeBytes("bbb".toByteArray()) }
        assertNotEquals(ModelManager.sha256(f1), ModelManager.sha256(f2))
    }

    @Test
    fun sha256_largeFile_doesNotThrow() {
        val file = tmp.newFile("large.bin")
        file.writeBytes(ByteArray(1024 * 1024) { it.toByte() }) // 1 MB
        assertNotNull(ModelManager.sha256(file))
    }

    // WhisperModel enum sanity

    @Test
    fun whisperModel_downloadUrls_containFilename() {
        for (model in WhisperModel.entries) {
            val url = WhisperModel.downloadUrl(model)
            assertTrue("URL for ${model.id} should contain filename",
                url.endsWith(model.filename))
        }
    }

    @Test
    fun whisperModel_sha256_are64Chars() {
        for (model in WhisperModel.entries) {
            assertEquals("SHA256 for ${model.id} should be 64 hex chars",
                64, model.sha256.length)
        }
    }

    @Test
    fun whisperModel_minRamBytes_ascending() {
        assertTrue(WhisperModel.TINY.minRamBytes < WhisperModel.BASE.minRamBytes)
        assertTrue(WhisperModel.BASE.minRamBytes < WhisperModel.SMALL.minRamBytes)
    }

    private fun sha256Hex(data: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256")
        d.update(data)
        return d.digest().joinToString("") { "%02x".format(it) }
    }
}
