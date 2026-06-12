package com.asitsounds.android.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asitsounds.android.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScaffold(title: String, onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        content = content
    )
}

@Composable
fun AccessibilityScreen(onBack: () -> Unit) {
    DetailScaffold("접근성", onBack) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TalkBack · 글꼴 크기 · 고대비 · 모션 줄이기는 시스템 설정을 따릅니다.",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("세그먼트는 타임코드 + 영문 + 한글 발음 + 번역을 결합해 낭독됩니다 (KWCAG/WCAG 준용).",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Text("터치 타깃 48dp 이상 보장.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    DetailScaffold("앱 정보", onBack) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column {
                    AboutRow("버전", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    HorizontalDivider()
                    AboutRow("빌드", BuildConfig.FLAVOR.ifEmpty { BuildConfig.BUILD_TYPE })
                    HorizontalDivider()
                    AboutRow("애플리케이션 ID", BuildConfig.APPLICATION_ID)
                }
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Text(value, color = MaterialTheme.colorScheme.outline) }
    )
}

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val items = listOf(
        "whisper.cpp — MIT (ggerganov)",
        "Whisper 모델(ggml) — MIT",
        "Jetpack Compose · Room · WorkManager · Hilt — Apache-2.0 (Google)",
        "Media3 / ExoPlayer — Apache-2.0 (Google)",
        "ML Kit Translation — Google Terms",
        "OkHttp — Apache-2.0 (Square)",
        "AndroidX Security (EncryptedSharedPreferences) — Apache-2.0"
    )
    DetailScaffold("오픈소스 라이선스", onBack) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(items) { line ->
                ListItem(headlineContent = { Text(line, style = MaterialTheme.typography.bodyMedium) })
                HorizontalDivider()
            }
        }
    }
}
