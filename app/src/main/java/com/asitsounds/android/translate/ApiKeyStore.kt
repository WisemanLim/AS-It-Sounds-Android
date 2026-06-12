package com.asitsounds.android.translate

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// API keys stored encrypted (Android Keystore-backed). Never logged, never plaintext.
class ApiKeyStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "translation_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getKey(engine: TranslationEngine): String? = prefs.getString(engine.id, null)

    fun setKey(engine: TranslationEngine, key: String) {
        prefs.edit().putString(engine.id, key).apply()
    }

    fun clearKey(engine: TranslationEngine) {
        prefs.edit().remove(engine.id).apply()
    }

    fun hasKey(engine: TranslationEngine): Boolean = !getKey(engine).isNullOrBlank()
}
