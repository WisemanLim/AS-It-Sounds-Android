package com.asitsounds.android.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asitsounds.android.core.data.ProjectRepository
import com.asitsounds.android.core.data.db.ProjectEntity
import com.asitsounds.android.core.data.db.ProjectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow(StatusFilter.ALL)

    val queryText: StateFlow<String> = query.asStateFlow()
    val filter: StateFlow<StatusFilter> = statusFilter.asStateFlow()

    val projects: StateFlow<List<ProjectEntity>> =
        combine(repository.observeProjects(), query, statusFilter) { list, q, f ->
            LibraryFilter.apply(list, q, f)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(value: String) { query.value = value }
    fun setFilter(value: StatusFilter) { statusFilter.value = value }

    fun retryProject(project: ProjectEntity) {
        viewModelScope.launch {
            val file = repository.getFirstFileForProject(project.id) ?: return@launch
            repository.clearSegmentsForFile(file.id)
            repository.updateFileStatus(file.id, ProjectStatus.PENDING, null)
            repository.updateProjectStatus(project.id, ProjectStatus.PENDING)
        }
    }
}
