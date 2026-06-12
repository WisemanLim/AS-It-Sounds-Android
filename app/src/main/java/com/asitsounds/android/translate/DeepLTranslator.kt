package com.asitsounds.android.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Requires a DeepL API key (free or pro). Key stored via EncryptedSharedPreferences.
class DeepLTranslator(
    private val apiKey: String,
    private val pro: Boolean = false,
    private val client: OkHttpClient = OkHttpClient()
) : TranslatorInterface {

    override suspend fun translate(text: String, sourceLang: String, targetLang: String): String =
        withContext(Dispatchers.IO) {
            if (text.isBlank() || apiKey.isBlank()) return@withContext ""
            val form = FormBody.Builder()
                .add("text", text)
                .add("target_lang", targetCode(targetLang))
            val req = Request.Builder()
                .url(endpoint(pro))
                .addHeader("Authorization", "DeepL-Auth-Key $apiKey")
                .post(form.build())
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext ""
                parseResponse(resp.body?.string().orEmpty())
            }
        }

    companion object {
        const val FREE_ENDPOINT = "https://api-free.deepl.com/v2/translate"
        const val PRO_ENDPOINT  = "https://api.deepl.com/v2/translate"

        internal fun endpoint(pro: Boolean) = if (pro) PRO_ENDPOINT else FREE_ENDPOINT

        // DeepL uses uppercase ISO codes; Korean = "KO"
        internal fun targetCode(lang: String): String = lang.uppercase()

        internal fun parseResponse(json: String): String {
            if (json.isBlank()) return ""
            return try {
                val arr = JSONObject(json).optJSONArray("translations") ?: return ""
                if (arr.length() == 0) return ""
                arr.getJSONObject(0).optString("text").orEmpty()
            } catch (e: Exception) {
                ""
            }
        }
    }
}
