package com.asitsounds.android.translate

import okhttp3.OkHttpClient

// Builds the active translator and wraps it with the ML Kit → MyMemory → off fallback chain.
class TranslatorFactory(
    private val apiKeyProvider: (TranslationEngine) -> String? = { null },
    private val client: OkHttpClient = OkHttpClient(),
    private val mlKitProvider: () -> TranslatorInterface = { MlKitTranslator() }
) {

    fun create(engine: TranslationEngine): TranslatorInterface = when (engine) {
        TranslationEngine.OFF            -> NoOpTranslator
        TranslationEngine.MLKIT          -> mlKitProvider()
        TranslationEngine.MYMEMORY       -> MyMemoryTranslator(client)
        TranslationEngine.LIBRETRANSLATE -> LibreTranslateTranslator(
            apiKey = apiKeyProvider(TranslationEngine.LIBRETRANSLATE),
            client = client
        )
        TranslationEngine.DEEPL          -> DeepLTranslator(
            apiKey = apiKeyProvider(TranslationEngine.DEEPL).orEmpty(),
            client = client
        )
    }

    // Wraps a primary translator: empty result → try fallback (used for ML Kit → MyMemory).
    fun withFallback(
        primary: TranslatorInterface,
        fallback: TranslatorInterface
    ): TranslatorInterface = object : TranslatorInterface {
        override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
            val result = runCatching { primary.translate(text, sourceLang, targetLang) }.getOrDefault("")
            return if (result.isNotBlank()) result
                   else runCatching { fallback.translate(text, sourceLang, targetLang) }.getOrDefault("")
        }
    }
}
