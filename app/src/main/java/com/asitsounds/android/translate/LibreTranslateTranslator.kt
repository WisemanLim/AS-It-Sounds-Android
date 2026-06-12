package com.asitsounds.android.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// Self-hostable open-source translation. Optional API key for hosted instances.
class LibreTranslateTranslator(
    private val baseUrl: String = DEFAULT_BASE,
    private val apiKey: String? = null,
    private val client: OkHttpClient = OkHttpClient()
) : TranslatorInterface {

    override suspend fun translate(text: String, sourceLang: String, targetLang: String): String =
        withContext(Dispatchers.IO) {
            if (text.isBlank()) return@withContext ""
            val body = buildBody(text, sourceLang, targetLang, apiKey)
                .toRequestBody(JSON_MEDIA)
            val req = Request.Builder().url("$baseUrl/translate").post(body).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext ""
                parseResponse(resp.body?.string().orEmpty())
            }
        }

    companion object {
        const val DEFAULT_BASE = "https://libretranslate.com"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

        internal fun srcCode(lang: String): String =
            if (lang.isBlank() || lang == "auto") "auto" else lang.take(2)

        internal fun buildBody(text: String, source: String, target: String, apiKey: String?): String {
            val obj = JSONObject()
                .put("q", text)
                .put("source", srcCode(source))
                .put("target", target)
                .put("format", "text")
            if (!apiKey.isNullOrBlank()) obj.put("api_key", apiKey)
            return obj.toString()
        }

        internal fun parseResponse(json: String): String {
            if (json.isBlank()) return ""
            return try {
                JSONObject(json).optString("translatedText").orEmpty()
            } catch (e: Exception) {
                ""
            }
        }
    }
}
