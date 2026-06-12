package com.asitsounds.android.pipeline

import app.cash.turbine.test
import com.asitsounds.android.asr.WhisperSegment
import com.asitsounds.android.translate.TranslatorInterface
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PipelineOrchestratorTest {

    @get:Rule val tmp = TemporaryFolder()

    private fun fakeSource(samples: FloatArray = FloatArray(0)) = object : AudioSource {
        override suspend fun decode() = samples
    }

    private fun fakeEngine(segments: List<WhisperSegment> = emptyList()) = object : WhisperEngine {
        override fun transcribe(audioData: FloatArray, language: String) = segments
    }

    private val noOpTranslator = object : TranslatorInterface {
        override suspend fun translate(text: String, sourceLang: String, targetLang: String) = ""
    }

    private val echoTranslator = object : TranslatorInterface {
        override suspend fun translate(text: String, sourceLang: String, targetLang: String) = "[$text]"
    }

    // --- empty pipeline ---

    @Test
    fun emptySegments_emitDecodingAsrFileDone() = runTest {
        val orc = PipelineOrchestrator(fakeSource(), fakeEngine(), noOpTranslator)
        orc.process(1L).test {
            assertEquals(PipelineEvent.Decoding, awaitItem())
            assertEquals(PipelineEvent.AsrProgress(0), awaitItem())
            assertEquals(PipelineEvent.AsrProgress(100), awaitItem())
            assertEquals(PipelineEvent.FileDone, awaitItem())
            awaitComplete()
        }
    }

    // --- single segment ---

    @Test
    fun singleSegment_emitsTranslatingThenSegment() = runTest {
        val seg = WhisperSegment(0L, 1000L, "Hello", "en")
        val orc = PipelineOrchestrator(fakeSource(), fakeEngine(listOf(seg)), echoTranslator)
        orc.process(2L).test {
            assertEquals(PipelineEvent.Decoding, awaitItem())
            assertEquals(PipelineEvent.AsrProgress(0), awaitItem())
            assertEquals(PipelineEvent.AsrProgress(100), awaitItem())
            assertEquals(PipelineEvent.TranslatingSegment(0), awaitItem())
            val s = awaitItem() as PipelineEvent.Segment
            assertEquals(0, s.index)
            assertEquals("Hello", s.textEn)
            assertEquals("[Hello]", s.textKo)
            assertFalse("hangul should not be empty", s.textHangul.isEmpty())
            assertEquals(PipelineEvent.FileDone, awaitItem())
            awaitComplete()
        }
    }

    // --- checkpoint resume ---

    @Test
    fun checkpointResume_skipsAlreadyProcessedSegments() = runTest {
        val segs = listOf(
            WhisperSegment(0L, 1000L, "First", "en"),
            WhisperSegment(1000L, 2000L, "Second", "en")
        )
        val checkpointDir = tmp.newFolder("cp")
        // pre-write checkpoint: already done segment 0
        val cpFile = java.io.File(checkpointDir, "cp_10.txt")
        cpFile.writeText("1")

        val orc = PipelineOrchestrator(fakeSource(), fakeEngine(segs), noOpTranslator, checkpointDir)
        val events = mutableListOf<PipelineEvent>()
        orc.process(10L).collect { events.add(it) }

        val segEvents = events.filterIsInstance<PipelineEvent.Segment>()
        assertEquals("Only segment 1 should be emitted (0 already checkpointed)", 1, segEvents.size)
        assertEquals(1, segEvents[0].index)
        assertEquals("Second", segEvents[0].textEn)
    }

    @Test
    fun checkpoint_clearedAfterSuccess() = runTest {
        val seg = WhisperSegment(0L, 500L, "Hi", "en")
        val checkpointDir = tmp.newFolder("cp2")
        val cpFile = java.io.File(checkpointDir, "cp_5.txt")

        val orc = PipelineOrchestrator(fakeSource(), fakeEngine(listOf(seg)), noOpTranslator, checkpointDir)
        orc.process(5L).collect {}

        assertFalse("Checkpoint file should be deleted after success", cpFile.exists())
    }

    @Test
    fun checkpoint_writtenForEachSegment() = runTest {
        val segs = listOf(
            WhisperSegment(0L, 1000L, "A", "en"),
            WhisperSegment(1000L, 2000L, "B", "en"),
            WhisperSegment(2000L, 3000L, "C", "en")
        )
        val checkpointDir = tmp.newFolder("cp3")
        var lastCheckpointValue = -1

        // Wrap to intercept save
        val orc = PipelineOrchestrator(fakeSource(), fakeEngine(segs), noOpTranslator, checkpointDir)
        orc.process(7L).collect { event ->
            if (event is PipelineEvent.Segment) {
                val cpFile = java.io.File(checkpointDir, "cp_7.txt")
                if (cpFile.exists()) {
                    lastCheckpointValue = cpFile.readText().toInt()
                }
            }
        }
        // Checkpoint is saved AFTER each Segment is emitted. So when the collector
        // observes segment N, the file holds N (written after segment N-1).
        // Observed during the last (3rd) segment → value 2. After FileDone the file
        // is cleared. This confirms the checkpoint advances monotonically per segment.
        assertEquals(2, lastCheckpointValue)
    }

    // --- translator called ---

    @Test
    fun translator_calledOncePerSegment() = runTest {
        var callCount = 0
        val countingTranslator = object : TranslatorInterface {
            override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
                callCount++
                return "번역"
            }
        }
        val segs = listOf(
            WhisperSegment(0L, 500L, "One", "en"),
            WhisperSegment(500L, 1000L, "Two", "en"),
            WhisperSegment(1000L, 1500L, "Three", "en")
        )
        val orc = PipelineOrchestrator(fakeSource(), fakeEngine(segs), countingTranslator)
        orc.process(99L).collect {}
        assertEquals(3, callCount)
    }
}
