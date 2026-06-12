package com.asitsounds.android.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asitsounds.android.core.ui.theme.ThemeMode
import com.asitsounds.android.translate.TranslationEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenModelManager: () -> Unit = {},
    onOpenTranslationEngine: () -> Unit = {},
    onOpenAccessibility: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val theme by viewModel.themeMode.collectAsState()
    val engine by viewModel.engine.collectAsState()
    val autoplay by viewModel.autoplay.collectAsState()
    val speed by viewModel.defaultSpeed.collectAsState()
    val model by viewModel.selectedModel.collectAsState()
    val realAsr by viewModel.useRealAsr.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("설정") }) }) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 모델 ---
            Section("모델") {
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            leadingContent = { Icon(Icons.Filled.Memory, contentDescription = null) },
                            headlineContent = { Text("모델 관리") },
                            supportingContent = { Text("${model.id} (${model.sizeBytes / 1_000_000} MB)") },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.clickable { onOpenModelManager() }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("실제 음성 인식 (whisper.cpp)") },
                            supportingContent = { Text(if (realAsr) "실제 추론" else "데모(Mock) 모드") },
                            trailingContent = { Switch(checked = realAsr, onCheckedChange = viewModel::setUseRealAsr) }
                        )
                    }
                }
            }

            // --- 번역 ---
            Section("번역") {
                Card(Modifier.fillMaxWidth()) {
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Translate, contentDescription = null) },
                        headlineContent = { Text("번역 엔진") },
                        supportingContent = { Text(engineLabelShort(engine)) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                        modifier = Modifier.clickable { onOpenTranslationEngine() }
                    )
                }
            }

            // --- 동기화 ---
            Section("동기화") {
                Card(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Google Drive 백업") },
                        supportingContent = { Text("프로젝트·설정 동기화 (모델·키 제외)") },
                        trailingContent = { Text("Auto Backup", color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            // --- 학습 환경 ---
            Section("학습 환경") {
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("다음 세그먼트 자동 재생") },
                            trailingContent = { Switch(checked = autoplay, onCheckedChange = viewModel::setAutoplay) }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("기본 속도 %.2g×".format(speed)) },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledTonalIconButton(onClick = { viewModel.setDefaultSpeed((speed - 0.25f).coerceIn(0.5f, 2f)) }) {
                                        Icon(Icons.Filled.Remove, contentDescription = "느리게")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    FilledTonalIconButton(onClick = { viewModel.setDefaultSpeed((speed + 0.25f).coerceIn(0.5f, 2f)) }) {
                                        Icon(Icons.Filled.Add, contentDescription = "빠르게")
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                        ThemeModeRow(theme, viewModel::setThemeMode)
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("접근성") },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.clickable { onOpenAccessibility() }
                        )
                    }
                }
            }

            // --- 정보 ---
            Section("정보") {
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("앱 정보") },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.clickable { onOpenAbout() }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("오픈소스 라이선스") },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                            modifier = Modifier.clickable { onOpenLicenses() }
                        )
                    }
                }
            }
        }
    }
}

// iOS-style select box (dropdown) for the display mode.
@Composable
private fun ThemeModeRow(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text("화면 모드") },
        trailingContent = {
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(themeLabel(current))
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ThemeMode.entries.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(themeLabel(m)) },
                            onClick = { onSelect(m); expanded = false }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
        content()
    }
}

private fun engineLabelShort(e: TranslationEngine): String = when (e) {
    TranslationEngine.MLKIT -> "ML Kit (온디바이스)"
    TranslationEngine.MYMEMORY -> "MyMemory"
    TranslationEngine.LIBRETRANSLATE -> "LibreTranslate"
    TranslationEngine.DEEPL -> "DeepL"
    TranslationEngine.OFF -> "끄기"
}

private fun themeLabel(m: ThemeMode): String = when (m) {
    ThemeMode.SYSTEM -> "시스템"
    ThemeMode.LIGHT -> "라이트"
    ThemeMode.DARK -> "다크"
}
