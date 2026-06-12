package com.asitsounds.android.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asitsounds.android.asr.WhisperModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    onBack: () -> Unit,
    viewModel: ModelManagerViewModel = hiltViewModel()
) {
    val rows by viewModel.rows.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("모델 관리") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column {
                    rows.forEachIndexed { i, row ->
                        if (i > 0) HorizontalDivider()
                        ListItem(
                            leadingContent = {
                                if (row.selected)
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "선택됨",
                                        tint = MaterialTheme.colorScheme.primary)
                                else
                                    Icon(Icons.Outlined.Circle, contentDescription = null)
                            },
                            headlineContent = { Text(row.model.id) },
                            supportingContent = { Text(modelDesc(row.model)) },
                            trailingContent = {
                                when {
                                    row.downloading -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                    row.cached -> Icon(Icons.Filled.CloudDone, contentDescription = "다운로드됨",
                                        tint = MaterialTheme.colorScheme.primary)
                                    else -> IconButton(onClick = { viewModel.download(row.model) }) {
                                        Icon(Icons.Filled.Download, contentDescription = "다운로드")
                                    }
                                }
                            },
                            modifier = Modifier.clickable { viewModel.select(row.model) }
                        )
                    }
                }
            }
            Text("기기 RAM에 맞는 모델이 자동 추천됩니다. tiny=빠름, base=균형(기본), small=정확.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun modelDesc(m: WhisperModel): String = when (m) {
    WhisperModel.TINY  -> "빠름 · 4GB 기기 · ${m.sizeBytes / 1_000_000}MB"
    WhisperModel.BASE  -> "균형 · 기본값 · ${m.sizeBytes / 1_000_000}MB"
    WhisperModel.SMALL -> "정확 · 8GB 기기 · ${m.sizeBytes / 1_000_000}MB"
}
