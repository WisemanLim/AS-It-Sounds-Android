package com.asitsounds.android.core.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.asitsounds.android.asr.WhisperModel
import com.asitsounds.android.core.ui.theme.ThemeMode
import com.asitsounds.android.translate.TranslationEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ENGINE = stringPreferencesKey("translation_engine")
        val AUTOPLAY = booleanPreferencesKey("autoplay")
        val SPEED = floatPreferencesKey("default_speed")
        // Onboarding is shown whenever this differs from the package's lastUpdateTime
        // → first install, every app update, and every dev reinstall.
        val SEEN_UPDATE_TIME = longPreferencesKey("onboarding_seen_update_time")
        val MODEL = stringPreferencesKey("selected_model")
        val REAL_ASR = booleanPreferencesKey("real_asr")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        runCatching { ThemeMode.valueOf(it[Keys.THEME] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM)
    }

    val engine: Flow<TranslationEngine> = context.dataStore.data.map {
        TranslationEngine.fromId(it[Keys.ENGINE])
    }

    val autoplay: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTOPLAY] ?: false }
    val defaultSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.SPEED] ?: 1.0f }
    val seenUpdateTime: Flow<Long> = context.dataStore.data.map { it[Keys.SEEN_UPDATE_TIME] ?: -1L }

    val selectedModel: Flow<WhisperModel> = context.dataStore.data.map { prefs ->
        WhisperModel.entries.firstOrNull { it.id == prefs[Keys.MODEL] } ?: WhisperModel.BASE
    }

    // false (default) = Mock ASR for dev; true = real whisper.cpp inference.
    val useRealAsr: Flow<Boolean> = context.dataStore.data.map { it[Keys.REAL_ASR] ?: false }

    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME] = mode.name }.let {}

    suspend fun setEngine(engine: TranslationEngine) =
        context.dataStore.edit { it[Keys.ENGINE] = engine.id }.let {}

    suspend fun setAutoplay(value: Boolean) =
        context.dataStore.edit { it[Keys.AUTOPLAY] = value }.let {}

    suspend fun setDefaultSpeed(value: Float) =
        context.dataStore.edit { it[Keys.SPEED] = value }.let {}

    suspend fun setSeenUpdateTime(time: Long) =
        context.dataStore.edit { it[Keys.SEEN_UPDATE_TIME] = time }.let {}

    suspend fun setSelectedModel(model: WhisperModel) =
        context.dataStore.edit { it[Keys.MODEL] = model.id }.let {}

    suspend fun setUseRealAsr(value: Boolean) =
        context.dataStore.edit { it[Keys.REAL_ASR] = value }.let {}
}
