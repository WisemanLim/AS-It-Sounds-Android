package com.asitsounds.android.translate

import org.junit.Assert.*
import org.junit.Test

class TranslatorParsingTest {

    // --- MyMemory ---

    @Test
    fun myMemory_parseResponse_extractsTranslatedText() {
        val json = """{"responseData":{"translatedText":"안녕하세요","match":1},"responseStatus":200}"""
        assertEquals("안녕하세요", MyMemoryTranslator.parseResponse(json))
    }

    @Test
    fun myMemory_parseResponse_emptyJson_returnsEmpty() {
        assertEquals("", MyMemoryTranslator.parseResponse(""))
    }

    @Test
    fun myMemory_parseResponse_malformed_returnsEmpty() {
        assertEquals("", MyMemoryTranslator.parseResponse("not json"))
    }

    @Test
    fun myMemory_parseResponse_missingField_returnsEmpty() {
        assertEquals("", MyMemoryTranslator.parseResponse("""{"responseStatus":403}"""))
    }

    @Test
    fun myMemory_langCode_autoMapsToEn() {
        assertEquals("en", MyMemoryTranslator.langCode("auto"))
        assertEquals("en", MyMemoryTranslator.langCode(""))
    }

    @Test
    fun myMemory_langCode_truncatesToTwo() {
        assertEquals("ja", MyMemoryTranslator.langCode("japanese"))
        assertEquals("fr", MyMemoryTranslator.langCode("fr"))
    }

    @Test
    fun myMemory_buildUrl_containsQueryAndLangpair() {
        val url = MyMemoryTranslator.buildUrl("hello", "en", "ko")
        assertTrue(url.contains("q=hello"))
        assertTrue(url.contains("langpair=en"))
        assertTrue(url.contains("api.mymemory.translated.net"))
    }

    @Test
    fun myMemory_buildUrl_encodesSpaces() {
        val url = MyMemoryTranslator.buildUrl("hello world", "en", "ko")
        assertTrue("Should URL-encode the space", url.contains("hello%20world") || url.contains("hello+world"))
    }

    // --- LibreTranslate ---

    @Test
    fun libre_parseResponse_extractsTranslatedText() {
        val json = """{"translatedText":"세계"}"""
        assertEquals("세계", LibreTranslateTranslator.parseResponse(json))
    }

    @Test
    fun libre_parseResponse_empty_returnsEmpty() {
        assertEquals("", LibreTranslateTranslator.parseResponse(""))
    }

    @Test
    fun libre_buildBody_includesRequiredFields() {
        val body = LibreTranslateTranslator.buildBody("hi", "en", "ko", null)
        assertTrue(body.contains("\"q\""))
        assertTrue(body.contains("\"source\""))
        assertTrue(body.contains("\"target\""))
        assertTrue(body.contains("\"ko\""))
    }

    @Test
    fun libre_buildBody_omitsApiKeyWhenNull() {
        val body = LibreTranslateTranslator.buildBody("hi", "en", "ko", null)
        assertFalse(body.contains("api_key"))
    }

    @Test
    fun libre_buildBody_includesApiKeyWhenPresent() {
        val body = LibreTranslateTranslator.buildBody("hi", "en", "ko", "secret123")
        assertTrue(body.contains("api_key"))
        assertTrue(body.contains("secret123"))
    }

    @Test
    fun libre_srcCode_autoStaysAuto() {
        assertEquals("auto", LibreTranslateTranslator.srcCode("auto"))
        assertEquals("auto", LibreTranslateTranslator.srcCode(""))
    }

    // --- DeepL ---

    @Test
    fun deepl_parseResponse_extractsFirstTranslation() {
        val json = """{"translations":[{"detected_source_language":"EN","text":"번역됨"}]}"""
        assertEquals("번역됨", DeepLTranslator.parseResponse(json))
    }

    @Test
    fun deepl_parseResponse_emptyArray_returnsEmpty() {
        assertEquals("", DeepLTranslator.parseResponse("""{"translations":[]}"""))
    }

    @Test
    fun deepl_parseResponse_malformed_returnsEmpty() {
        assertEquals("", DeepLTranslator.parseResponse("garbage"))
    }

    @Test
    fun deepl_endpoint_selectsByPlan() {
        assertEquals(DeepLTranslator.FREE_ENDPOINT, DeepLTranslator.endpoint(false))
        assertEquals(DeepLTranslator.PRO_ENDPOINT, DeepLTranslator.endpoint(true))
    }

    @Test
    fun deepl_targetCode_uppercases() {
        assertEquals("KO", DeepLTranslator.targetCode("ko"))
    }

    // --- ML Kit normalize ---

    @Test
    fun mlkit_normalize_autoMapsToEn() {
        assertEquals("en", MlKitTranslator.normalize("auto"))
        assertEquals("en", MlKitTranslator.normalize(""))
    }

    @Test
    fun mlkit_normalize_truncates() {
        assertEquals("ja", MlKitTranslator.normalize("japanese"))
    }
}
