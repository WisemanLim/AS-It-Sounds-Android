package com.asitsounds.android.translate

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class TranslatorFactoryTest {

    private val factory = TranslatorFactory(
        mlKitProvider = { object : TranslatorInterface {
            override suspend fun translate(text: String, sourceLang: String, targetLang: String) = "mlkit:$text"
        } }
    )

    @Test
    fun create_off_returnsNoOp() {
        assertSame(NoOpTranslator, factory.create(TranslationEngine.OFF))
    }

    @Test
    fun create_mlkit_returnsProvided() = runTest {
        val t = factory.create(TranslationEngine.MLKIT)
        assertEquals("mlkit:hi", t.translate("hi", "en"))
    }

    @Test
    fun create_mymemory_returnsMyMemory() {
        assertTrue(factory.create(TranslationEngine.MYMEMORY) is MyMemoryTranslator)
    }

    @Test
    fun create_libre_returnsLibre() {
        assertTrue(factory.create(TranslationEngine.LIBRETRANSLATE) is LibreTranslateTranslator)
    }

    @Test
    fun create_deepl_returnsDeepL() {
        assertTrue(factory.create(TranslationEngine.DEEPL) is DeepLTranslator)
    }

    // --- fallback chain ---

    private val emptyTranslator = object : TranslatorInterface {
        override suspend fun translate(text: String, sourceLang: String, targetLang: String) = ""
    }
    private val goodTranslator = object : TranslatorInterface {
        override suspend fun translate(text: String, sourceLang: String, targetLang: String) = "FALLBACK"
    }
    private val throwingTranslator = object : TranslatorInterface {
        override suspend fun translate(text: String, sourceLang: String, targetLang: String): String =
            throw RuntimeException("network down")
    }

    @Test
    fun withFallback_primarySucceeds_usesPrimary() = runTest {
        val primary = object : TranslatorInterface {
            override suspend fun translate(text: String, sourceLang: String, targetLang: String) = "PRIMARY"
        }
        val wrapped = factory.withFallback(primary, goodTranslator)
        assertEquals("PRIMARY", wrapped.translate("x", "en"))
    }

    @Test
    fun withFallback_primaryEmpty_usesFallback() = runTest {
        val wrapped = factory.withFallback(emptyTranslator, goodTranslator)
        assertEquals("FALLBACK", wrapped.translate("x", "en"))
    }

    @Test
    fun withFallback_primaryThrows_usesFallback() = runTest {
        val wrapped = factory.withFallback(throwingTranslator, goodTranslator)
        assertEquals("FALLBACK", wrapped.translate("x", "en"))
    }

    @Test
    fun withFallback_bothFail_returnsEmpty() = runTest {
        val wrapped = factory.withFallback(throwingTranslator, emptyTranslator)
        assertEquals("", wrapped.translate("x", "en"))
    }

    // --- engine enum ---

    @Test
    fun engine_fromId_knownIds() {
        assertEquals(TranslationEngine.DEEPL, TranslationEngine.fromId("deepl"))
        assertEquals(TranslationEngine.OFF, TranslationEngine.fromId("off"))
    }

    @Test
    fun engine_fromId_unknownDefaultsToMlkit() {
        assertEquals(TranslationEngine.MLKIT, TranslationEngine.fromId("nonsense"))
        assertEquals(TranslationEngine.MLKIT, TranslationEngine.fromId(null))
    }

    @Test
    fun engine_onlyDeeplRequiresKey() {
        assertTrue(TranslationEngine.DEEPL.requiresApiKey)
        assertFalse(TranslationEngine.MLKIT.requiresApiKey)
        assertFalse(TranslationEngine.MYMEMORY.requiresApiKey)
    }
}
