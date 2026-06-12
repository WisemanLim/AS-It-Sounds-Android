package com.asitsounds.android.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Free, key-less translation API. Used as the ML Kit fallback.
class MyMemoryTranslator(
    private val client: OkHttpClient = OkHttpClient()
) : TranslatorInterface {

    override suspend fun translate(text: String, sourceLang: String, targetLang: String): String =
        withContext(Dispatchers.IO) {
            if (text.isBlank()) return@withContext ""
            val req = Request.Builder().url(buildUrl(text, sourceLang, targetLang)).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext ""
                parseResponse(resp.body?.string().orEmpty())
            }
        }

    companion object {
        private const val BASE = "https://api.mymemory.translated.net/get"

        internal fun buildUrl(text: String, source: String, target: String): String =
            BASE.toHttpUrl().newBuilder()
                .addQueryParameter("q", text)
                .addQueryParameter("langpair", "${langCode(source)}|$target")
                .build()
                .toString()

        // MyMemory wants 2-letter codes; map whisper "auto"/unknowns to "en"
        internal fun langCode(lang: String): String =
            if (lang.isBlank() || lang == "auto") "en" else lang.take(2)

        internal fun parseResponse(json: String): String {
            if (json.isBlank()) return ""
            return try {
                JSONObject(json)
                    .optJSONObject("responseData")
                    ?.optString("translatedText")
                    .orEmpty()
            } catch (e: Exception) {
                ""
            }
        }
    }
}
