package com.asitsounds.android.feature.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asitsounds.android.asr.ModelManager
import com.asitsounds.android.asr.RamGuard
import com.asitsounds.android.asr.WhisperModel
import com.asitsounds.android.core.data.ProjectRepository
import com.asitsounds.android.core.data.db.MediaFileEntity
import com.asitsounds.android.core.data.db.ProjectEntity
import com.asitsounds.android.core.data.db.ProjectStatus
import com.asitsounds.android.core.data.db.SegmentEntity
import com.asitsounds.android.feature.import_.MediaImporter
import com.asitsounds.android.translit.TranslitEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelStatus(val model: WhisperModel, val cached: Boolean) {
    val sizeMb: Int get() = (model.sizeBytes / 1_000_000).toInt()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProjectRepository
) : ViewModel() {

    val recentProjects: StateFlow<List<ProjectEntity>> =
        repository.observeRecent(3)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val modelManager = ModelManager(context)
    private val _modelStatus = MutableStateFlow(
        ModelStatus(RamGuard.recommendedModel(context), false)
    )
    val modelStatus: StateFlow<ModelStatus> = _modelStatus.asStateFlow()

    init {
        val m = RamGuard.recommendedModel(context)
        _modelStatus.value = ModelStatus(m, modelManager.isCached(m))
    }

    // Newly-created project id, surfaced so the UI can navigate to its detail screen.
    private val _createdProjectId = MutableStateFlow<Long?>(null)
    val createdProjectId: StateFlow<Long?> = _createdProjectId.asStateFlow()
    fun consumeCreatedProject() { _createdProjectId.value = null }

    fun importUri(uri: Uri) {
        viewModelScope.launch {
            val imported = runCatching { MediaImporter(context).import(uri) }.getOrNull() ?: return@launch
            val now = System.currentTimeMillis()
            val projectId = repository.createProject(imported.displayName, now)
            repository.addFile(
                MediaFileEntity(
                    projectId = projectId,
                    path = imported.path,
                    displayName = imported.displayName,
                    mime = imported.mime,
                    durationMs = 0,
                    createdAt = now
                )
            )
            _createdProjectId.value = projectId
        }
    }

    // Dev demo: synthesize segments instantly (no audio/ASR) using the real
    // TranslitEngine for the Hangul line. Lets the full UI flow be tested without files.
    fun startMockTranscription() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val name = "데모 " + (now % 100000).toString().padStart(5, '0')
            val projectId = repository.createProject(name, now)
            val fileId = repository.addFile(
                MediaFileEntity(
                    projectId = projectId, path = "mock://demo", displayName = "$name.mock",
                    mime = "audio/mock", durationMs = 11_000, status = ProjectStatus.DONE, createdAt = now
                )
            )
            val sentences = listOf(
                "Exercise." to "훈련",
                "Number one." to "1번",
                "You wanted these charts copied." to "이 차트를 복사하길 원하셨죠.",
                "And then they should be refiled." to "그런 다음 다시 정리되어야 합니다.",
                "As it sounds, it works." to "들리는 대로, 작동합니다."
            )
            val segs = sentences.mapIndexed { i, (en, ko) ->
                SegmentEntity(
                    fileId = fileId, idx = i,
                    startMs = i * 2000L, endMs = (i + 1) * 2000L,
                    textEn = en,
                    textHangul = TranslitEngine.transliterate(en),
                    textKo = ko
                )
            }
            repository.addSegments(segs)
            repository.updateProject(
                repository.getProject(projectId)!!.copy(status = ProjectStatus.DONE, updatedAt = now)
            )
            _createdProjectId.value = projectId
        }
    }
}
