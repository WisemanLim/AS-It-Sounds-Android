package com.asitsounds.android.feature.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asitsounds.android.asr.RollingBuffer
import com.asitsounds.android.platform.RealtimeSttController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RealtimeState(
    val recording: Boolean = false,
    val seconds: Int = 0,
    val windowSamples: Int = 0
)

@HiltViewModel
class RealtimeViewModel @Inject constructor() : ViewModel() {

    private val controller = RealtimeSttController()
    private var job: Job? = null

    private val _state = MutableStateFlow(RealtimeState())
    val state: StateFlow<RealtimeState> = _state.asStateFlow()

    fun start() {
        if (job != null) return
        _state.value = RealtimeState(recording = true)
        job = viewModelScope.launch {
            controller.start().collect { snapshot ->
                _state.value = _state.value.copy(
                    seconds = snapshot.size / RollingBuffer.SAMPLE_RATE,
                    windowSamples = snapshot.size
                )
            }
        }
    }

    fun stop() {
        job?.cancel(); job = null
        _state.value = _state.value.copy(recording = false)
    }

    override fun onCleared() { stop() }
}
