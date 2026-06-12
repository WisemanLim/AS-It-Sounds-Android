package com.asitsounds.android.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asitsounds.android.core.data.ProjectRepository
import com.asitsounds.android.core.data.db.SegmentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val projectId = MutableStateFlow(-1L)
    private val segmentFilter = MutableStateFlow(SegmentFilter.ALL)
    private val position = MutableStateFlow(0L)

    val filter: StateFlow<SegmentFilter> = segmentFilter.asStateFlow()
    val positionMs: StateFlow<Long> = position.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    // project → its first media file → that file's segments
    @OptIn(ExperimentalCoroutinesApi::class)
    val segments: StateFlow<List<SegmentEntity>> =
        projectId.flatMapLatest { pid ->
            if (pid < 0) flowOf(emptyList())
            else repository.observeFiles(pid).flatMapLatest { files ->
                val fileId = files.firstOrNull()?.id
                if (fileId == null) flowOf(emptyList()) else repository.observeSegments(fileId)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeIndex: StateFlow<Int> =
        combine(segments, position) { segs, pos ->
            PlayerLogic.activeSegmentIndex(pos, segs.map { it.startMs }, segs.map { it.endMs })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    fun load(projectId: Long) {
        this.projectId.value = projectId
        viewModelScope.launch { _title.value = repository.getProject(projectId)?.name ?: "" }
    }

    fun setFilter(f: SegmentFilter) { segmentFilter.value = f }
    fun updatePosition(ms: Long) { position.value = ms }
}
