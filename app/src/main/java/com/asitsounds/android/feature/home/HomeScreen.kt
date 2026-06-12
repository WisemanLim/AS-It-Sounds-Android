package com.asitsounds.android.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asitsounds.android.BuildConfig
import com.asitsounds.android.core.data.db.ProjectEntity
import com.asitsounds.android.feature.import_.ImportSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewTranscription: () -> Unit,   // retained for callers; not used directly
    onOpenProject: (Long) -> Unit,
    onOpenModelManager: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenRealtime: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recent by viewModel.recentProjects.collectAsState()
    val model by viewModel.modelStatus.collectAsState()
    val createdId by viewModel.createdProjectId.collectAsState()
    var showImport by remember { mutableStateOf(false) }

    // Navigate to the project once import+creation finishes.
    LaunchedEffect(createdId) {
        createdId?.let { id -> viewModel.consumeCreatedProject(); onOpenProject(id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("As It Sounds") },
                navigationIcon = {
                    IconButton(onClick = onOpenRealtime) {
                        Icon(Icons.Filled.Mic, contentDescription = "실시간 전사")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "설정")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // New transcription card
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("새 전사 시작", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showImport = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("파일 가져오기")
                    }
                }
            }

            // Speech-recognition model status card
            Card(onClick = onOpenModelManager, modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    leadingContent = { Icon(Icons.Filled.Memory, contentDescription = null) },
                    headlineContent = { Text("음성 인식 모델") },
                    supportingContent = {
                        Text("${model.model.id} (${model.sizeMb} MB)" +
                            if (model.cached) " · 다운로드됨" else " · 미다운로드")
                    },
                    trailingContent = { Icon(Icons.Filled.GraphicEq, contentDescription = null) }
                )
            }

            Text("최근 프로젝트", style = MaterialTheme.typography.titleMedium)
            if (recent.isEmpty()) {
                Text("아직 프로젝트가 없습니다.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recent) { project -> RecentRow(project, onOpenProject) }
                }
            }
        }
    }

    if (showImport) {
        ImportSheet(
            onDismiss = { showImport = false },
            onUriPicked = { uri, _ -> showImport = false; viewModel.importUri(uri) },
            onDemo = if (BuildConfig.DEBUG) {
                { showImport = false; viewModel.startMockTranscription() }
            } else null
        )
    }
}

@Composable
private fun RecentRow(project: ProjectEntity, onOpen: (Long) -> Unit) {
    Card(onClick = { onOpen(project.id) }, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { Icon(Icons.Filled.GraphicEq, contentDescription = null) },
            headlineContent = { Text(project.name) },
            supportingContent = { Text(project.status.name) }
        )
    }
}
