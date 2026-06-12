package com.asitsounds.android.translit

// TranslitEngine: multi-language phoneme → Korean Hangul transliterator.
// 1:1 Kotlin port of iOS Transliterator.swift (which itself ports desktop translit.rs).
// Routing: Latin(en/fr/es/de) · Kana(ja) · Hangul(passthrough) · Cyrillic(ru) · CJK/other(passthrough).
object TranslitEngine {

    // ─── Hangul composition ───────────────────────────────────────────────────
    private fun compose(onset: Int, vowel: Int, coda: Int): Char =
        (0xAC00 + (onset * 21 + vowel) * 28 + coda).toChar()

    private const val NO_CODA = 0
    private const val C_NG = 21  // ㅇ jongseong
    private const val C_L  = 8   // ㄹ
    private const val C_M  = 16  // ㅁ
    private const val C_N  = 4   // ㄴ
    private const val C_K  = 1   // ㄱ
    private const val C_T  = 19  // ㅅ (loanword -t)
    private const val C_P  = 17  // ㅂ

    private enum class Vowel(val idx: Int) {
        A(0), Ae(1), Ya(2), Eo(4), E(5), Yeo(6),
        O(8), Wa(9), Oe(11), Yo(12), U(13),
        Wi(16), Yu(17), Eu(18), Ui(19), I(20)
    }

    private fun push(out: MutableList<Char>, onset: Int, v: Vowel, coda: Int) {
        out.add(compose(onset, v.idx, coda))
    }

    private fun onsetToCoda(onset: Int): Int? = when (onset) {
        0, 15 -> C_K
        2     -> C_N
        3, 16 -> C_T
        5     -> C_L
        6     -> C_M
        7, 17 -> C_P
        9     -> C_T
        else  -> null
    }

    private fun tryAttachCoda(out: MutableList<Char>, onset: Int): Boolean {
        val last = out.removeLastOrNull() ?: return false
        val cu = last.code
        if (cu in 0xAC00..0xD7A3 && (cu - 0xAC00) % 28 == 0) {
            val coda = onsetToCoda(onset)
            if (coda != null) { out.add((cu + coda).toChar()); return true }
        }
        out.add(last)
        return false
    }

    private fun isVowelChar(c: Char): Boolean = when (c.lowercaseChar()) {
        'a', 'e', 'i', 'o', 'u', 'y' -> true
        else -> false
    }

    // ─── Script detection ─────────────────────────────────────────────────────
    private enum class Script { LATIN, KANA, HANGUL, CYRILLIC, CJK, OTHER }

    private fun charScript(c: Char): Script {
        val cp = c.code
        return when {
            cp in 0x3040..0x309F || cp in 0x30A0..0x30FF -> Script.KANA
            cp in 0xAC00..0xD7AF || cp in 0x1100..0x11FF || cp in 0x3130..0x318F -> Script.HANGUL
            cp in 0x0400..0x04FF -> Script.CYRILLIC
            cp in 0x4E00..0x9FFF || cp in 0x3400..0x4DBF -> Script.CJK
            cp in 0x0021..0x007E || cp in 0x00C0..0x024F -> Script.LATIN
            else -> Script.OTHER
        }
    }

    private fun dominantScript(text: String): Script {
        val counts = IntArray(5)
        for (c in text) {
            if (!c.isLetter()) continue
            when (charScript(c)) {
                Script.LATIN    -> counts[0]++
                Script.KANA     -> counts[1]++
                Script.HANGUL   -> counts[2]++
                Script.CYRILLIC -> counts[3]++
                Script.CJK      -> counts[4]++
                Script.OTHER    -> Unit
            }
        }
        // Rust max_by_key returns LAST max on ties → use >=
        var maxIdx = 0
        for (i in 1..4) if (counts[i] >= counts[maxIdx]) maxIdx = i
        return arrayOf(Script.LATIN, Script.KANA, Script.HANGUL, Script.CYRILLIC, Script.CJK)[maxIdx]
    }

    // ─── Japanese kana → Korean ───────────────────────────────────────────────
    private fun kataToHira(c: Char): Char {
        val cp = c.code
        return if (cp in 0x30A1..0x30F6) (cp - 0x60).toChar() else c
    }

    private val kanaMap: Map<String, String> = mapOf(
        "きゃ" to "캬", "きゅ" to "큐", "きょ" to "쿄",
        "ぎゃ" to "갸", "ぎゅ" to "규", "ぎょ" to "교",
        "しゃ" to "샤", "しゅ" to "슈", "しょ" to "쇼",
        "じゃ" to "자", "じゅ" to "주", "じょ" to "조",
        "ちゃ" to "차", "ちゅ" to "추", "ちょ" to "초",
        "にゃ" to "냐", "にゅ" to "뉴", "にょ" to "뇨",
        "ひゃ" to "햐", "ひゅ" to "휴", "ひょ" to "효",
        "びゃ" to "뱌", "びゅ" to "뷰", "びょ" to "뵤",
        "ぴゃ" to "퍄", "ぴゅ" to "퓨", "ぴょ" to "표",
        "みゃ" to "먀", "みゅ" to "뮤", "みょ" to "묘",
        "りゃ" to "랴", "りゅ" to "류", "りょ" to "료",
        "あ" to "아", "い" to "이", "う" to "우", "え" to "에", "お" to "오",
        "か" to "카", "き" to "키", "く" to "쿠", "け" to "케", "こ" to "코",
        "が" to "가", "ぎ" to "기", "ぐ" to "구", "げ" to "게", "ご" to "고",
        "さ" to "사", "し" to "시", "す" to "스", "せ" to "세", "そ" to "소",
        "ざ" to "자", "じ" to "지", "ず" to "즈", "ぜ" to "제", "ぞ" to "조",
        "た" to "타", "ち" to "치", "つ" to "츠", "て" to "테", "と" to "토",
        "だ" to "다", "ぢ" to "지", "づ" to "즈", "で" to "데", "ど" to "도",
        "な" to "나", "に" to "니", "ぬ" to "누", "ね" to "네", "の" to "노",
        "は" to "하", "ひ" to "히", "ふ" to "후", "へ" to "헤", "ほ" to "호",
        "ば" to "바", "び" to "비", "ぶ" to "부", "べ" to "베", "ぼ" to "보",
        "ぱ" to "파", "ぴ" to "피", "ぷ" to "푸", "ぺ" to "페", "ぽ" to "포",
        "ま" to "마", "み" to "미", "む" to "무", "め" to "메", "も" to "모",
        "や" to "야", "ゆ" to "유", "よ" to "요",
        "ら" to "라", "り" to "리", "る" to "루", "れ" to "레", "ろ" to "로",
        "わ" to "와", "ゐ" to "이", "ゑ" to "에", "を" to "오"
    )

    private fun kanaSentenceToHangul(text: String): String {
        val chars = text.map(::kataToHira)
        val n = chars.size
        val out = mutableListOf<Char>()
        var i = 0
        while (i < n) {
            val c = chars[i]
            if (c.code < 128) { out.add(c); i++; continue }
            if (c == 'ん') {
                val last = out.removeLastOrNull()
                if (last != null) {
                    val cu = last.code
                    if (cu in 0xAC00..0xD7A3 && (cu - 0xAC00) % 28 == 0) {
                        out.add((cu + C_NG).toChar())
                    } else { out.add(last); out.addAll("응".toList()) }
                } else out.addAll("응".toList())
                i++; continue
            }
            if (c == 'っ') {
                val last = out.removeLastOrNull()
                if (last != null) {
                    val cu = last.code
                    if (cu in 0xAC00..0xD7A3 && (cu - 0xAC00) % 28 == 0) {
                        out.add((cu + C_T).toChar())
                    } else out.add(last)
                }
                i++; continue
            }
            if (i + 1 < n) {
                val two = "${chars[i]}${chars[i + 1]}"
                val h = kanaMap[two]
                if (h != null) { out.addAll(h.toList()); i += 2; continue }
            }
            val h = kanaMap[c.toString()]
            if (h != null) out.addAll(h.toList()) else out.add(c)
            i++
        }
        return out.joinToString("")
    }

    // ─── Cyrillic → Korean ────────────────────────────────────────────────────
    private fun cyrIsVowel(c: Char): Boolean = "аАеЕёЁиИйЙоОуУэЭюЮяЯ".contains(c)

    private fun cyrVowelToHangul(c: Char): Pair<Vowel, Boolean> = when (c) {
        'а', 'А' -> Vowel.A  to false
        'е', 'Е' -> Vowel.E  to true
        'ё', 'Ё' -> Vowel.Yo to true
        'и', 'И' -> Vowel.I  to false
        'й', 'Й' -> Vowel.I  to false
        'о', 'О' -> Vowel.O  to false
        'у', 'У' -> Vowel.U  to false
        'э', 'Э' -> Vowel.E  to false
        'ю', 'Ю' -> Vowel.Yu to true
        'я', 'Я' -> Vowel.Ya to true
        else      -> Vowel.Eu to false
    }

    private fun cyrOnset(c: Char): Int? = when (c.lowercaseChar()) {
        'г', 'к'             -> 0
        'н'                  -> 2
        'д', 'т'             -> 3
        'л', 'р'             -> 5
        'м'                  -> 6
        'б', 'в', 'п', 'ф'  -> 7
        'с', 'з', 'ш', 'щ', 'ж' -> 9
        'ц', 'ч'             -> 14
        'х'                  -> 18
        else                 -> null
    }

    private fun cyrillicWordToHangul(word: String): String {
        val chars = word.toList()
        val n = chars.size
        val out = mutableListOf<Char>()
        var pending: Int? = null

        fun flush() {
            pending?.let { onset ->
                pending = null
                if (!tryAttachCoda(out, onset)) push(out, onset, Vowel.Eu, NO_CODA)
            }
        }

        var i = 0
        while (i < n) {
            val c = chars[i]
            if (c == 'ъ' || c == 'ь' || c == 'Ъ' || c == 'Ь') { i++; continue }
            if (cyrIsVowel(c)) {
                val (vowel, yGlide) = cyrVowelToHangul(c)
                val onset = pending ?: 11; pending = null
                if (yGlide && onset == 11) push(out, 11, vowel, NO_CODA)
                else push(out, onset, vowel, NO_CODA)
            } else {
                val cons = cyrOnset(c)
                if (cons != null) {
                    flush()
                    if (i + 1 == n) {
                        if (!tryAttachCoda(out, cons)) push(out, cons, Vowel.Eu, NO_CODA)
                    } else pending = cons
                } else if (!c.isLetter()) {
                    flush(); out.add(c)
                }
            }
            i++
        }
        flush()
        return out.joinToString("")
    }

    private fun cyrillicSentenceToHangul(sentence: String): String {
        val (words, seps) = splitMixed(sentence)
        val result = StringBuilder()
        for (k in words.indices) {
            result.append(cyrillicWordToHangul(words[k]))
            if (k < seps.size) result.append(seps[k])
        }
        return result.toString()
    }

    // ─── Latin language detection ─────────────────────────────────────────────
    enum class LatinLang { ENGLISH, FRENCH, SPANISH, GERMAN }

    fun detectLatinLang(sentence: String): LatinLang {
        if (sentence.any { "äöüß".contains(it) }) return LatinLang.GERMAN
        if (sentence.any { "ñáíóú¿¡".contains(it) }) return LatinLang.SPANISH
        if (sentence.any { "àâçèéêëîïôùûœæ".contains(it) }) return LatinLang.FRENCH
        return LatinLang.ENGLISH
    }

    // ─── English rules ────────────────────────────────────────────────────────
    private fun onsetForEn(c: Char): Int? = when (c) {
        'g', 'G' -> 0;  'n', 'N' -> 2;  'd', 'D' -> 3
        'r', 'R', 'l', 'L' -> 5
        'm', 'M' -> 6;  'b', 'B', 'v', 'V' -> 7
        's', 'S', 'z', 'Z' -> 9
        'j', 'J' -> 12; 'c', 'C' -> 14
        'k', 'K', 'q', 'Q', 'x', 'X' -> 15
        't', 'T' -> 16; 'p', 'P', 'f', 'F' -> 17
        'h', 'H' -> 18; 'w', 'W', 'y', 'Y' -> 11
        else -> null
    }

    fun ruleTransliterateEn(word: String): String {
        val chars = word.lowercase().toList()
        val n = chars.size
        if (n == 0) return ""
        val out = mutableListOf<Char>()
        var i = 0
        var pending: Int? = null

        fun ch(j: Int): Char? = if (j in 0 until n) chars[j] else null
        fun isAtVowel(j: Int) = ch(j)?.let(::isVowelChar) ?: false
        fun slice(a: Int, len: Int) = chars.drop(a).take(len).joinToString("")
        fun flushPending() {
            pending?.let { p -> pending = null
                if (!tryAttachCoda(out, p)) push(out, p, Vowel.Eu, NO_CODA)
            }
        }

        while (i < n) {
            val s3 = slice(i, 3)
            val s2 = slice(i, 2)

            // ── 3-char patterns ───────────────────────────────────────────────
            if (s3 == "tch") { flushPending(); pending = 14; i += 3; continue }
            if (s3 == "igh" || s3 == "eye") {
                flushPending()
                push(out, 11, Vowel.A, NO_CODA); push(out, 11, Vowel.I, NO_CODA)
                i += 3; continue
            }

            // ── word-initial "re" ─────────────────────────────────────────────
            if (i == 0 && s2 == "re" && n > 3) {
                push(out, 5, Vowel.I, NO_CODA); pending = null; i += 2; continue
            }

            // ── "we" at end or before apostrophe ──────────────────────────────
            if (s2 == "we" && (i + 2 == n || ch(i + 2) == '\'')) {
                val o = pending ?: 11; pending = null
                push(out, o, Vowel.Wi, NO_CODA); i += 2; continue
            }

            // ── "-ed" at end after consonant ──────────────────────────────────
            if (i + 2 == n && chars[i] == 'e' && chars[i + 1] == 'd'
                && i > 0 && !isVowelChar(chars[i - 1])) {
                pending?.let { p -> pending = null; push(out, p, Vowel.Eu, NO_CODA) }
                push(out, 3, Vowel.Eu, NO_CODA); i += 2; continue
            }

            // ── Vowel digraphs ────────────────────────────────────────────────
            when (s2) {
                "er", "ir", "ur" -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.Eo, NO_CODA); i += 2; continue }
                "ar"             -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.A, NO_CODA); i += 2; continue }
                "or"             -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.O, NO_CODA); i += 2; continue }
                "oo"             -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.U, NO_CODA); i += 2; continue }
                "ee", "ea", "ei" -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.I, NO_CODA); i += 2; continue }
                "ai", "ay"       -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.E, NO_CODA); push(out, 11, Vowel.I, NO_CODA); i += 2; continue }
                "ou", "ow"       -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.A, NO_CODA); push(out, 11, Vowel.U, NO_CODA); i += 2; continue }
                "oi", "oy"       -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.O, NO_CODA); push(out, 11, Vowel.I, NO_CODA); i += 2; continue }
                "au", "aw"       -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.O, NO_CODA); i += 2; continue }
                "ie"             -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.I, NO_CODA); i += 2; continue }
                "ew"             -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.Yu, NO_CODA); i += 2; continue }
                "ue"             -> { val o = pending ?: 11; pending = null; push(out, o, Vowel.U, NO_CODA); i += 2; continue }
            }

            // ── Consonant digraphs ────────────────────────────────────────────
            when (s2) {
                "th" -> { flushPending(); pending = 9; i += 2; continue }
                "sh" -> { flushPending(); push(out, 9, Vowel.Wi, NO_CODA); i += 2; continue }
                "ch" -> { flushPending(); pending = 14; i += 2; continue }
                "ph" -> { flushPending(); pending = 17; i += 2; continue }
                "gh" -> { i += 2; continue }
                "ck" -> {
                    flushPending()
                    val last = out.removeLastOrNull()
                    if (last != null) {
                        val cu = last.code
                        if (cu in 0xAC00..0xD7A3 && (cu - 0xAC00) % 28 == 0) {
                            out.add((cu + C_K).toChar()); i += 2; continue
                        }
                        out.add(last)
                    }
                    pending = 15; i += 2; continue
                }
                "qu" -> { flushPending(); push(out, 15, Vowel.U, NO_CODA); i += 2; continue }
                "wh" -> { flushPending(); i += 1; continue }
                "ng" -> {
                    flushPending()
                    val last = out.removeLastOrNull()
                    if (last != null) {
                        val cu = last.code
                        if (cu in 0xAC00..0xD7A3 && (cu - 0xAC00) % 28 == 0) {
                            out.add((cu + C_NG).toChar()); i += 2; continue
                        }
                        out.add(last)
                    }
                    push(out, 11, Vowel.Eu, C_NG); i += 2; continue
                }
            }

            val c = chars[i]

            // ── Single vowel ──────────────────────────────────────────────────
            if (isVowelChar(c)) {
                // Special cases that exit early (continue outer loop)
                if (c == 'a') {
                    if (i + 2 == n && chars[i + 1] == 'e') {
                        val o = pending ?: 11; pending = null
                        push(out, o, Vowel.E, NO_CODA); push(out, 11, Vowel.I, NO_CODA)
                        i += 2; continue
                    }
                    if (i + 2 < n && !isVowelChar(chars[i + 1]) && chars[i + 1] != 'r' && isVowelChar(chars[i + 2])) {
                        val o = pending ?: 11; pending = null
                        push(out, o, Vowel.E, NO_CODA); push(out, 11, Vowel.I, NO_CODA)
                        i += 1; continue
                    }
                }
                if (c == 'e' && i + 1 == n) {
                    pending?.let { p -> pending = null; push(out, p, Vowel.Eu, NO_CODA) }
                    i += 1; continue
                }
                if (c == 'i' && i + 2 < n && !isAtVowel(i + 1) && chars[i + 2] == 'e') {
                    val o = pending ?: 11; pending = null
                    push(out, o, Vowel.A, NO_CODA); push(out, 11, Vowel.I, NO_CODA)
                    i += 1; continue
                }
                if (c == 'y' && i == 0 && i + 1 < n && isAtVowel(i + 1)) { i += 1; continue }

                val vowel = when (c) {
                    'a'  -> Vowel.A
                    'e'  -> Vowel.E
                    'i'  -> Vowel.I
                    'o'  -> Vowel.O
                    'u'  -> Vowel.Eo
                    'y'  -> Vowel.I
                    else -> Vowel.Eu
                }
                val onset = pending ?: 11; pending = null
                push(out, onset, vowel, NO_CODA); i += 1; continue
            }

            // ── Single consonant ──────────────────────────────────────────────
            val consBase = onsetForEn(c)
            if (consBase != null) {
                var cons = consBase
                if (c == 'c') cons = if (ch(i + 1)?.let { "eiy".contains(it) } == true) 9 else 15
                else if (c == 'g') cons = if (ch(i + 1).let { it == 'e' || it == 'i' }) 12 else 0

                flushPending()
                if (i + 1 == n) {
                    val last = out.removeLastOrNull()
                    if (last != null) {
                        val cu = last.code
                        if (cu in 0xAC00..0xD7A3 && (cu - 0xAC00) % 28 == 0) {
                            val coda = when (c) {
                                'n'       -> C_N
                                'm'       -> C_M
                                'l', 'r'  -> C_L
                                'k', 'g'  -> C_K
                                't', 'd', 's', 'z' -> C_T
                                'p', 'b'  -> C_P
                                else      -> null
                            }
                            if (coda != null) { out.add((cu + coda).toChar()); i += 1; continue }
                        }
                        out.add(last)
                    }
                    push(out, cons, Vowel.Eu, NO_CODA); i += 1; continue
                }
                pending = cons
            }
            i += 1
        }
        pending?.let { p -> push(out, p, Vowel.Eu, NO_CODA) }
        return out.joinToString("")
    }

    // ─── French / Spanish / German shared onset ───────────────────────────────
    private fun onsetForLatin(c: Char): Int? = when (c.lowercaseChar()) {
        'g'      -> 0;  'n' -> 2;  'd' -> 3
        'r', 'l' -> 5;  'm' -> 6;  'b', 'v' -> 7
        's', 'z' -> 9;  'j' -> 12; 'c' -> 14
        'k', 'q' -> 15; 't' -> 16; 'p', 'f' -> 17
        'h'      -> 18; 'w', 'y' -> 11
        else     -> null
    }

    // ─── French ───────────────────────────────────────────────────────────────
    fun ruleTransliterateFr(word: String): String {
        val chars: List<Char> = word.map { c ->
            when (c) {
                'À', 'Â', 'à', 'â'                   -> 'a'
                'È', 'É', 'Ê', 'Ë', 'è', 'é', 'ê', 'ë' -> 'e'
                'Î', 'Ï', 'î', 'ï'                   -> 'i'
                'Ô', 'ô'                              -> 'o'
                'Ù', 'Û', 'Ü', 'ù', 'û', 'ü'         -> 'u'
                'Ç', 'ç'                              -> 's'
                'Œ', 'œ'                              -> 'e'
                'Æ', 'æ'                              -> 'a'
                else                                  -> c.lowercaseChar()
            }
        }
        val n = chars.size
        val out = mutableListOf<Char>()
        var pending: Int? = null
        var i = 0

        fun flush() {
            pending?.let { onset -> pending = null
                if (!tryAttachCoda(out, onset)) push(out, onset, Vowel.Eu, NO_CODA)
            }
        }
        fun peek(j: Int): Char? = if (j in 0 until n) chars[j] else null
        fun beforeConsOrEnd(j: Int) = peek(j)?.let { !isVowelChar(it) } ?: true
        fun sliceAt(j: Int, len: Int) = chars.drop(j).take(len).joinToString("")

        while (i < n) {
            val s4 = sliceAt(i, 4); val s3 = sliceAt(i, 3); val s2 = sliceAt(i, 2)
            val c = chars[i]

            if (s4 == "tion") { flush(); push(out, 9, Vowel.I, NO_CODA); push(out, 11, Vowel.O, C_NG); i += 4; continue }
            if (s3 == "eau") { val o = pending ?: 11; pending = null; push(out, o, Vowel.O, NO_CODA); i += 3; continue }
            if (s2 == "gn") { flush(); push(out, 2, Vowel.Yu, NO_CODA); i += 2; continue }
            if (s2 == "ch") { flush(); push(out, 9, Vowel.Wi, NO_CODA); i += 2; continue }
            if (s2 == "ph") { flush(); pending = 17; i += 2; continue }
            if (s2 == "ou") { val o = pending ?: 11; pending = null; push(out, o, Vowel.U, NO_CODA); i += 2; continue }
            if (s2 == "au") { val o = pending ?: 11; pending = null; push(out, o, Vowel.O, NO_CODA); i += 2; continue }
            if (s2 == "oi") { val o = pending ?: 11; pending = null; push(out, o, Vowel.Wa, NO_CODA); i += 2; continue }
            if (s2 == "ai" || s2 == "ei") { val o = pending ?: 11; pending = null; push(out, o, Vowel.E, NO_CODA); i += 2; continue }
            if (s2 == "eu") { val o = pending ?: 11; pending = null; push(out, o, Vowel.Eu, NO_CODA); i += 2; continue }
            if ((s2 == "an" || s2 == "am" || s2 == "en" || s2 == "em") && beforeConsOrEnd(i + 2)) {
                val o = pending ?: 11; pending = null; push(out, o, Vowel.A, C_NG); i += 2; continue
            }
            if ((s2 == "in" || s2 == "im") && beforeConsOrEnd(i + 2)) {
                val o = pending ?: 11; pending = null; push(out, o, Vowel.E, C_NG); i += 2; continue
            }
            if ((s2 == "on" || s2 == "om") && beforeConsOrEnd(i + 2)) {
                val o = pending ?: 11; pending = null; push(out, o, Vowel.O, C_NG); i += 2; continue
            }
            if ((s2 == "un" || s2 == "um") && beforeConsOrEnd(i + 2)) {
                val o = pending ?: 11; pending = null; push(out, o, Vowel.A, C_NG); i += 2; continue
            }
            if (i + 1 == n && "dtsxzp".contains(c)) { flush(); i += 1; continue }
            if (c == 'r') { flush(); pending = 18; i += 1; continue }

            if (isVowelChar(c)) {
                if (c == 'e' && i + 1 == n) { flush(); i += 1; continue }
                val vowel = when (c) {
                    'a'  -> Vowel.A
                    'e'  -> Vowel.E
                    'i'  -> Vowel.I
                    'o'  -> Vowel.O
                    'u'  -> Vowel.U
                    'y'  -> Vowel.I
                    else -> Vowel.Eu
                }
                val o = pending ?: 11; pending = null; push(out, o, vowel, NO_CODA)
            } else {
                val cons = onsetForLatin(c)
                if (cons != null) {
                    flush()
                    if (i + 1 == n) { if (!tryAttachCoda(out, cons)) push(out, cons, Vowel.Eu, NO_CODA) }
                    else pending = cons
                }
            }
            i += 1
        }
        flush()
        return out.joinToString("")
    }

    // ─── Spanish ──────────────────────────────────────────────────────────────
    fun ruleTransliterateEs(word: String): String {
        val chars: List<Char> = word.map { c ->
            when (c) {
                'Á', 'á'       -> 'a'
                'É', 'é'       -> 'e'
                'Í', 'í'       -> 'i'
                'Ó', 'ó'       -> 'o'
                'Ú', 'Ü', 'ú', 'ü' -> 'u'
                'ñ', 'Ñ'       -> c
                else           -> c.lowercaseChar()
            }
        }
        val n = chars.size
        val out = mutableListOf<Char>()
        var pending: Int? = null
        var i = 0

        fun flush() {
            pending?.let { onset -> pending = null
                if (!tryAttachCoda(out, onset)) push(out, onset, Vowel.Eu, NO_CODA)
            }
        }
        fun peek(j: Int): Char? = if (j in 0 until n) chars[j] else null
        fun sliceAt(j: Int, len: Int) = chars.drop(j).take(len).joinToString("")

        while (i < n) {
            val s2 = sliceAt(i, 2)
            val c = chars[i]

            if (c == 'ñ' || c == 'Ñ') { flush(); push(out, 2, Vowel.Yu, NO_CODA); i += 1; continue }
            if (s2 == "ll") { flush(); push(out, 11, Vowel.I, NO_CODA); i += 2; continue }
            if (s2 == "rr") { flush(); pending = 5; i += 2; continue }
            if (s2 == "ch") { flush(); pending = 14; i += 2; continue }
            if (s2 == "qu") { flush(); pending = 15; i += 2; continue }
            if (s2 == "gu" && (peek(i + 2)?.let { it == 'e' || it == 'i' } == true)) {
                flush(); pending = 0; i += 2; continue
            }
            if (c == 'h') { i += 1; continue }
            if (c == 'j' || (c == 'g' && (peek(i + 1)?.let { it == 'e' || it == 'i' } == true))) {
                flush(); pending = 18; i += 1; continue
            }
            if (c == 'v') { flush(); pending = 7; i += 1; continue }
            if (c == 'z' || (c == 'c' && (peek(i + 1)?.let { it == 'e' || it == 'i' } == true))) {
                flush(); pending = 9; i += 1; continue
            }

            if (isVowelChar(c)) {
                val vowel = when (c) {
                    'a'       -> Vowel.A
                    'e'       -> Vowel.E
                    'i', 'y'  -> Vowel.I
                    'o'       -> Vowel.O
                    'u'       -> Vowel.U
                    else      -> Vowel.Eu
                }
                val o = pending ?: 11; pending = null; push(out, o, vowel, NO_CODA)
            } else {
                val cons = onsetForLatin(c)
                if (cons != null) {
                    flush()
                    if (i + 1 == n) { if (!tryAttachCoda(out, cons)) push(out, cons, Vowel.Eu, NO_CODA) }
                    else pending = cons
                }
            }
            i += 1
        }
        flush()
        return out.joinToString("")
    }

    // ─── German ───────────────────────────────────────────────────────────────
    fun ruleTransliterateDe(word: String): String {
        val chars = word.toList()
        val n = chars.size
        val out = mutableListOf<Char>()
        var pending: Int? = null
        var i = 0

        fun flush() {
            pending?.let { onset -> pending = null
                if (!tryAttachCoda(out, onset)) push(out, onset, Vowel.Eu, NO_CODA)
            }
        }
        fun peek(j: Int): Char? = if (j in 0 until n) chars[j] else null
        fun sliceLc(j: Int, len: Int) = chars.drop(j).take(len).joinToString("") { it.lowercaseChar().toString() }

        while (i < n) {
            val cOrig = chars[i]
            val c = cOrig.lowercaseChar()
            val s3 = sliceLc(i, 3); val s2 = sliceLc(i, 2)

            if (s3 == "sch") { flush(); push(out, 9, Vowel.Wi, NO_CODA); i += 3; continue }
            if (s2 == "ch") {
                flush()
                val prevFront = out.lastOrNull()?.let { lc ->
                    val cp = lc.code
                    if (cp in 0xAC00..0xD7A3) { val v = ((cp - 0xAC00) / 28) % 21; v == 5 || v == 20 }
                    else false
                } ?: false
                if (prevFront) push(out, 18, Vowel.I, NO_CODA) else push(out, 18, Vowel.Eu, NO_CODA)
                i += 2; continue
            }
            if (s2 == "qu") { flush(); pending = 15; i += 2; continue }
            if ((s2 == "st" || s2 == "sp") && i == 0) {
                push(out, 9, Vowel.Wi, NO_CODA)
                pending = if (c == 's' && peek(i + 1) == 't') 16 else 17
                i += 2; continue
            }
            if (s2 == "ei") {
                val o = pending ?: 11; pending = null
                push(out, o, Vowel.A, NO_CODA); push(out, 11, Vowel.I, NO_CODA); i += 2; continue
            }
            if (s2 == "ie") { val o = pending ?: 11; pending = null; push(out, o, Vowel.I, NO_CODA); i += 2; continue }
            if (s2 == "eu" || s2 == "äu") {
                val o = pending ?: 11; pending = null
                push(out, o, Vowel.O, NO_CODA); push(out, 11, Vowel.I, NO_CODA); i += 2; continue
            }
            if (s2 == "au") {
                val o = pending ?: 11; pending = null
                push(out, o, Vowel.A, NO_CODA); push(out, 11, Vowel.U, NO_CODA); i += 2; continue
            }
            if (cOrig == 'ß') { flush(); push(out, 9, Vowel.Eu, NO_CODA); i += 1; continue }
            if (cOrig == 'ä' || cOrig == 'Ä') { val o = pending ?: 11; pending = null; push(out, o, Vowel.E, NO_CODA); i += 1; continue }
            if (cOrig == 'ö' || cOrig == 'Ö') { val o = pending ?: 11; pending = null; push(out, o, Vowel.Oe, NO_CODA); i += 1; continue }
            if (cOrig == 'ü' || cOrig == 'Ü') { val o = pending ?: 11; pending = null; push(out, o, Vowel.Wi, NO_CODA); i += 1; continue }
            if (c == 'w') { flush(); pending = 7; i += 1; continue }
            if (c == 'v') { flush(); pending = 17; i += 1; continue }
            if (c == 'z') { flush(); push(out, 14, Vowel.Eu, NO_CODA); i += 1; continue }
            if (c == 'i' && peek(i + 1) == 'g' && i + 2 == n) {
                val o = pending ?: 11; pending = null
                push(out, o, Vowel.I, NO_CODA); push(out, 18, Vowel.I, NO_CODA); i += 2; continue
            }

            if (isVowelChar(c)) {
                val vowel = when (c) {
                    'a'      -> Vowel.A
                    'e'      -> Vowel.E
                    'i', 'y' -> Vowel.I
                    'o'      -> Vowel.O
                    'u'      -> Vowel.U
                    else     -> Vowel.Eu
                }
                val o = pending ?: 11; pending = null; push(out, o, vowel, NO_CODA)
            } else {
                val cons = onsetForLatin(c)
                if (cons != null) {
                    flush()
                    if (i + 1 == n) { if (!tryAttachCoda(out, cons)) push(out, cons, Vowel.Eu, NO_CODA) }
                    else pending = cons
                }
            }
            i += 1
        }
        flush()
        return out.joinToString("")
    }

    // ─── English letter names ─────────────────────────────────────────────────
    private fun letterNameKo(c: Char): String? = when (c.uppercaseChar()) {
        'A' -> "에이"; 'B' -> "비";    'C' -> "씨"
        'D' -> "디";   'E' -> "이";    'F' -> "에프"
        'G' -> "지";   'H' -> "에이치"; 'I' -> "아이"
        'J' -> "제이"; 'K' -> "케이";  'L' -> "엘"
        'M' -> "엠";   'N' -> "엔";   'O' -> "오"
        'P' -> "피";   'Q' -> "큐";   'R' -> "아르"
        'S' -> "에스"; 'T' -> "티";   'U' -> "유"
        'V' -> "브이"; 'W' -> "더블유"; 'X' -> "엑스"
        'Y' -> "와이"; 'Z' -> "지"
        else -> null
    }

    // ─── Latin token dispatch ─────────────────────────────────────────────────
    private fun splitPunct(s: String): Triple<String, String, String> {
        val chars = s.toList()
        fun isWord(c: Char) = (c.code in 0..127 && c.isLetter()) || c == '\''
        val start = chars.indexOfFirst(::isWord).takeIf { it >= 0 } ?: chars.size
        val end = (chars.indexOfLast(::isWord).takeIf { it >= 0 } ?: -1) + 1
        if (start >= end) return Triple(s, "", "")
        return Triple(
            chars.take(start).joinToString(""),
            chars.subList(start, end).joinToString(""),
            chars.drop(end).joinToString("")
        )
    }

    private fun splitPunctUnicode(s: String): Triple<String, String, String> {
        val chars = s.toList()
        fun isWord(c: Char) = c.isLetter() || c == '\'' || c == '-'
        val start = chars.indexOfFirst(::isWord).takeIf { it >= 0 } ?: chars.size
        val end = (chars.indexOfLast(::isWord).takeIf { it >= 0 } ?: -1) + 1
        if (start >= end) return Triple(s, "", "")
        return Triple(
            chars.take(start).joinToString(""),
            chars.subList(start, end).joinToString(""),
            chars.drop(end).joinToString("")
        )
    }

    fun transliterateLatinToken(token: String, lang: LatinLang): String {
        val (prefix, core, suffix) = if (lang == LatinLang.ENGLISH) splitPunct(token) else splitPunctUnicode(token)
        if (core.isEmpty()) return token
        if (core.length == 1) {
            val firstC = core[0]
            if (firstC.code in 0..127 && firstC.isUpperCase()) {
                letterNameKo(firstC)?.let { name -> return "$prefix$name$suffix" }
            }
        }
        val lower = core.lowercase()
        val han = when (lang) {
            LatinLang.ENGLISH  -> TranslitDict.dict[lower] ?: ruleTransliterateEn(lower)
            LatinLang.FRENCH   -> ruleTransliterateFr(core)
            LatinLang.SPANISH  -> ruleTransliterateEs(core)
            LatinLang.GERMAN   -> ruleTransliterateDe(core)
        }
        return "$prefix$han$suffix"
    }

    // ─── Split preserving delimiters (Cyrillic) ───────────────────────────────
    private fun splitMixed(sentence: String): Pair<List<String>, List<String>> {
        val chars = sentence.toList()
        val words = mutableListOf<String>()
        val seps = mutableListOf<String>()
        var inWord = false
        var wordStart = 0
        var sepStart = 0
        for ((idx, c) in chars.withIndex()) {
            val isAlpha = c.isLetter()
            if (!inWord && isAlpha) {
                seps.add(chars.subList(sepStart, idx).joinToString("")); wordStart = idx; inWord = true
            } else if (inWord && !isAlpha && c != '\'' && c != '-') {
                words.add(chars.subList(wordStart, idx).joinToString("")); sepStart = idx; inWord = false
            }
        }
        if (inWord) { words.add(chars.drop(wordStart).joinToString("")); seps.add("") }
        else seps.add(chars.drop(sepStart).joinToString(""))
        return Pair(words, seps)
    }

    // ─── Main entry ───────────────────────────────────────────────────────────
    fun transliterate(sentence: String): String {
        if (sentence.trim().isEmpty()) return sentence
        return when (dominantScript(sentence)) {
            Script.KANA     -> kanaSentenceToHangul(sentence)
            Script.HANGUL   -> sentence
            Script.CYRILLIC -> cyrillicSentenceToHangul(sentence)
            Script.CJK, Script.OTHER -> sentence
            Script.LATIN    -> {
                val lang = detectLatinLang(sentence)
                sentence.trim().split(Regex("\\s+"))
                    .filter { it.isNotEmpty() }
                    .map { transliterateLatinToken(it, lang) }
                    .joinToString(" ")
            }
        }
    }
}
