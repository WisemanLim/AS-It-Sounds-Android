package com.asitsounds.android.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asitsounds.android.translate.TranslationEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationEngineScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val current by viewModel.engine.collectAsState()

    // iOS order: ML Kit / MyMemory / LibreTranslate / DeepL / Off
    val order = listOf(
        TranslationEngine.MLKIT,
        TranslationEngine.MYMEMORY,
        TranslationEngine.LIBRETRANSLATE,
        TranslationEngine.DEEPL,
        TranslationEngine.OFF
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("번역 엔진") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("기본 엔진", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
            Card(Modifier.fillMaxWidth()) {
                Column {
                    order.forEachIndexed { i, e ->
                        if (i > 0) HorizontalDivider()
                        ListItem(
                            headlineContent = { Text(engineLabel(e)) },
                            trailingContent = {
                                if (e == current)
                                    Icon(Icons.Filled.Check, contentDescription = "선택됨",
                                        tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.clickable { viewModel.setEngine(e) }
                        )
                    }
                }
            }
            Text("폴백", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
            Card(Modifier.fillMaxWidth()) {
                ListItem(headlineContent = { Text("ML Kit → MyMemory → 끄기") })
            }
            Text("API 키는 EncryptedSharedPreferences 에만 저장됩니다(평문/로그 미저장).",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun engineLabel(e: TranslationEngine): String = when (e) {
    TranslationEngine.MLKIT -> "ML Kit (온디바이스, 기본)"
    TranslationEngine.MYMEMORY -> "MyMemory"
    TranslationEngine.LIBRETRANSLATE -> "LibreTranslate"
    TranslationEngine.DEEPL -> "DeepL (API 키 필요)"
    TranslationEngine.OFF -> "끄기"
}
