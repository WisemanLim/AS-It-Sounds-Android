// as-it-sounds-android — Compose entrypoint
package com.asitsounds.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.asitsounds.android.core.ui.theme.AsItSoundsTheme
import com.asitsounds.android.feature.navigation.AppShell
import com.asitsounds.android.feature.onboarding.OnboardingPagerScreen
import com.asitsounds.android.feature.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val showOnboarding by settingsViewModel.shouldShowOnboarding.collectAsState()

    AsItSoundsTheme(themeMode = themeMode) {
        when (showOnboarding) {
            null -> Unit  // DataStore still loading — avoid onboarding flash
            true -> OnboardingPagerScreen(onFinish = { settingsViewModel.completeOnboarding() })
            false -> AppShell()
        }
    }
}
