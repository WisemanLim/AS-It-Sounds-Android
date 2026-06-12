package com.asitsounds.android.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asitsounds.android.core.data.SettingsRepository
import com.asitsounds.android.core.ui.theme.ThemeMode
import com.asitsounds.android.translate.TranslationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository
) : ViewModel() {

    // Changes on first install, every app update, and every dev reinstall.
    private val currentUpdateTime: Long = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
    }.getOrDefault(0L)

    val themeMode = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)
    val engine = settings.engine
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TranslationEngine.MLKIT)
    val autoplay = settings.autoplay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val defaultSpeed = settings.defaultSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)
    val selectedModel = settings.selectedModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.asitsounds.android.asr.WhisperModel.BASE)
    val useRealAsr = settings.useRealAsr
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // null = still loading; once DataStore emits → show if the seen-time differs from
    // the current package update time (first install / update / dev reinstall).
    val shouldShowOnboarding = settings.seenUpdateTime
        .map { seen -> seen != currentUpdateTime }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeOnboarding() = viewModelScope.launch { settings.setSeenUpdateTime(currentUpdateTime) }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setEngine(engine: TranslationEngine) = viewModelScope.launch { settings.setEngine(engine) }
    fun setAutoplay(value: Boolean) = viewModelScope.launch { settings.setAutoplay(value) }
    fun setDefaultSpeed(value: Float) = viewModelScope.launch { settings.setDefaultSpeed(value) }
    fun setUseRealAsr(value: Boolean) = viewModelScope.launch { settings.setUseRealAsr(value) }
}
