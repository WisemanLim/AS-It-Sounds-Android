package com.asitsounds.android.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asitsounds.android.asr.ModelManager
import com.asitsounds.android.asr.WhisperModel
import com.asitsounds.android.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelRow(
    val model: WhisperModel,
    val selected: Boolean,
    val cached: Boolean,
    val downloading: Boolean
)

@HiltViewModel
class ModelManagerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository
) : ViewModel() {

    private val manager = ModelManager(context)
    private val downloadingIds = MutableStateFlow<Set<String>>(emptySet())

    val rows: StateFlow<List<ModelRow>> =
        combine(settings.selectedModel, downloadingIds) { selected, downloading ->
            WhisperModel.entries.map { m ->
                ModelRow(
                    model = m,
                    selected = m == selected,
                    cached = manager.isCached(m),
                    downloading = m.id in downloading
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun select(model: WhisperModel) = viewModelScope.launch { settings.setSelectedModel(model) }

    fun download(model: WhisperModel) {
        downloadingIds.value = downloadingIds.value + model.id
        manager.enqueueDownload(model)
    }
}
