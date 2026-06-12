package com.asitsounds.android.translate

interface TranslatorInterface {
    suspend fun translate(text: String, sourceLang: String, targetLang: String = "ko"): String
}

object NoOpTranslator : TranslatorInterface {
    override suspend fun translate(text: String, sourceLang: String, targetLang: String) = ""
}

enum class TranslationEngine(val id: String, val requiresApiKey: Boolean) {
    OFF("off", false),
    MLKIT("mlkit", false),
    MYMEMORY("mymemory", false),
    LIBRETRANSLATE("libretranslate", false),
    DEEPL("deepl", true);

    companion object {
        fun fromId(id: String?): TranslationEngine =
            entries.firstOrNull { it.id == id } ?: MLKIT
    }
}
