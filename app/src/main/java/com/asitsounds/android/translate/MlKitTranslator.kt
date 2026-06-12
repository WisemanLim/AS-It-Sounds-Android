package com.asitsounds.android.translate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// On-device translation, no API key, no network after language-pack download. Default engine.
class MlKitTranslator : TranslatorInterface {

    override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        if (text.isBlank()) return ""
        val src = TranslateLanguage.fromLanguageTag(normalize(sourceLang))
            ?: TranslateLanguage.ENGLISH
        val tgt = TranslateLanguage.fromLanguageTag(targetLang)
            ?: TranslateLanguage.KOREAN

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(src)
            .setTargetLanguage(tgt)
            .build()
        val translator = Translation.getClient(options)

        return try {
            awaitTask(translator.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build())) { }
            awaitTask(translator.translate(text)) { it }
        } finally {
            translator.close()
        }
    }

    companion object {
        internal fun normalize(lang: String): String =
            if (lang.isBlank() || lang == "auto") "en" else lang.take(2)
    }

    private suspend fun <T, R> awaitTask(
        task: com.google.android.gms.tasks.Task<T>,
        map: (T) -> R
    ): R = suspendCancellableCoroutine { cont ->
        task.addOnSuccessListener { cont.resume(map(it)) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }
}
