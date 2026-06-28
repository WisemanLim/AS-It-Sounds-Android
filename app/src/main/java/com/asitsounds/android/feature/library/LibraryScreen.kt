package com.asitsounds.android.feature.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.asitsounds.android.core.data.db.ProjectStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    onOpenProject: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val projects by viewModel.projects.collectAsState()
    val query by viewModel.queryText.collectAsState()
    val filter by viewModel.filter.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("라이브러리") }) }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                label = { Text("검색") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects) { project ->
                    Card(onClick = { onOpenProject(project.id) }, modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text(project.name) },
                            supportingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(project.status.name)
                                    if (project.status == ProjectStatus.PROCESSING) {
                                        Spacer(Modifier.width(8.dp))
                                        LinearProgressIndicator(modifier = Modifier.weight(1f))
                                    }
                                }
                            },
                            trailingContent = {
                                if (project.status == ProjectStatus.ERROR ||
                                    project.status == ProjectStatus.PROCESSING) {
                                    TextButton(onClick = { viewModel.retryProject(project) }) {
                                        Text("재개")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
