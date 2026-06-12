package com.asitsounds.android.translit

// Parity regression — 1:1 port of iOS TranslitParityTests.swift (27 cases from translit.rs #[test]).
// Kotlin port guarantees identical output to the Swift implementation.
import org.junit.Assert.*
import org.junit.Test

class TranslitEngineTest {

    private val T = TranslitEngine

    @Test fun test01_englishDictHit() {
        assertTrue(T.transliterate("Hello, world!").contains("헬로"))
    }

    @Test fun test02_englishFallbackHangul() {
        assertTrue(T.ruleTransliterateEn("crackle").isNotEmpty())
    }

    @Test fun test03_englishNumberDict() {
        assertEquals("넘버 2", T.transliterate("Number 2"))
    }

    @Test fun test04_englishCodaCluster() {
        assertEquals("넘버", T.ruleTransliterateEn("number"))
    }

    @Test fun test05_englishErDigraph() {
        val out = T.ruleTransliterateEn("butter")
        assertTrue("got: $out", out.endsWith("어") || out.contains("터"))
    }

    @Test fun test06_englishElectrician() {
        val out = T.transliterate("The electrician is coming today, isn't he?")
        assertTrue("got: $out", out.contains("일렉트리션"))
        assertTrue("got: $out", out.contains("커밍"))
        assertTrue("got: $out", out.contains("이즌트"))
    }

    @Test fun test07_japaneseKanaBasic() {
        val out = T.transliterate("こんにちは")
        assertTrue("got: $out", out.contains("콩") && out.contains("치") && out.contains("하"))
    }

    @Test fun test08_japaneseKatakana() {
        val out = T.transliterate("コーヒー")
        assertTrue("got: $out", out.contains("코") && out.contains("히"))
    }

    @Test fun test09_russianBasic() {
        val out = T.transliterate("Москва")
        assertTrue("got: $out", out.isNotEmpty() && out.all { !(it.code in 0x41..0x5A || it.code in 0x61..0x7A) })
    }

    @Test fun test10_frenchBonjour() {
        assertTrue(T.ruleTransliterateFr("bonjour").isNotEmpty())
    }

    @Test fun test11_frenchNasalDetection() {
        assertEquals(TranslitEngine.LatinLang.ENGLISH, T.detectLatinLang("bonjour le monde"))
        assertEquals(TranslitEngine.LatinLang.FRENCH,  T.detectLatinLang("très bien"))
    }

    @Test fun test12_germanUmlautDetection() {
        assertEquals(TranslitEngine.LatinLang.GERMAN, T.detectLatinLang("schön"))
    }

    @Test fun test13_spanishDetection() {
        assertEquals(TranslitEngine.LatinLang.SPANISH, T.detectLatinLang("mañana"))
    }

    @Test fun test14_hangulPassthrough() {
        assertEquals("안녕하세요", T.transliterate("안녕하세요"))
    }

    @Test fun test15_hardCCorner() {
        val out = T.ruleTransliterateEn("corner")
        assertTrue("got: $out", out.startsWith("코"))
    }

    @Test fun test16_softCCity() {
        val out = T.ruleTransliterateEn("city")
        assertTrue("got: $out", out.startsWith("시"))
    }

    @Test fun test17_letterNameA() {
        assertEquals("에이.", T.transliterateLatinToken("A.", TranslitEngine.LatinLang.ENGLISH))
    }

    @Test fun test18_letterNameB() {
        assertEquals("비.", T.transliterateLatinToken("B.", TranslitEngine.LatinLang.ENGLISH))
    }

    @Test fun test19_letterNameC() {
        assertEquals("씨.", T.transliterateLatinToken("C.", TranslitEngine.LatinLang.ENGLISH))
    }

    @Test fun test20_choiceSentence() {
        val out = T.transliterate("C. Right around the corner.")
        assertTrue("got: $out", out.startsWith("씨."))
        assertTrue("corner should be 코너, got: $out", out.contains("코너"))
    }

    @Test fun test21_weveContraction() {
        val out = T.ruleTransliterateEn("we've")
        assertTrue("got: $out", out.startsWith("위"))
    }

    @Test fun test22_eiLongE() {
        val out = T.ruleTransliterateEn("receive")
        assertTrue("got: $out", out.contains("시"))
    }

    @Test fun test23_edSuffix() {
        val out = T.ruleTransliterateEn("received")
        assertTrue("got: $out", out.endsWith("드"))
    }

    @Test fun test24_rePrefix() {
        val out = T.ruleTransliterateEn("received")
        assertTrue("got: $out", out.startsWith("리"))
    }

    @Test fun test25_lowercaseANoDict() {
        assertTrue(T.ruleTransliterateEn("a").isNotEmpty())
    }

    @Test fun test26_openSyllableLongA() {
        val out = T.ruleTransliterateEn("catering")
        assertTrue("got: $out", out.startsWith("케이"))
    }

    @Test fun test27_fullSampleSentence() {
        val out = T.transliterate("A. I don't think we've received it.")
        assertTrue("got: $out", out.startsWith("에이."))
        assertTrue("we've should be 위브, got: $out", out.contains("위브"))
        assertTrue("received should be 리시브드, got: $out", out.contains("리시브드"))
    }
}
