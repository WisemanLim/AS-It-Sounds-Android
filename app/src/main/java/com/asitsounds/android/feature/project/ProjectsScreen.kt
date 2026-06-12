package com.asitsounds.android.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onOpenProject: (Long) -> Unit,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val projects by viewModel.projects.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("프로젝트") }) }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            Text("최근", style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 8.dp))
            if (projects.isEmpty()) {
                Text("아직 프로젝트가 없습니다.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projects) { project ->
                        Card(onClick = { onOpenProject(project.id) }, modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(project.name) },
                                supportingContent = { Text(project.status.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}
