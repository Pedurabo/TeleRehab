package com.signaldesk.telerehab.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signaldesk.telerehab.domain.analysis.PoseAnalysisEngine
import com.signaldesk.telerehab.domain.analysis.PoseFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GuidedSessionAnalysisState(
    val framesSubmitted: Long = 0,
    val framesAnalyzed: Long = 0,
    val lastLandmarkCount: Int = 0,
    val isAnalyzing: Boolean = false,
)

@HiltViewModel
class GuidedSessionViewModel @Inject constructor(
    private val poseAnalysisEngine: PoseAnalysisEngine,
) : ViewModel() {

    private val _analysisState =
        MutableStateFlow(
            GuidedSessionAnalysisState(),
        )

    val analysisState:
        StateFlow<GuidedSessionAnalysisState> =
        _analysisState

    fun submitFrame(
        frame: PoseFrame,
    ) {
        val current =
            _analysisState.value

        _analysisState.value =
            current.copy(
                framesSubmitted =
                    current.framesSubmitted + 1,
            )

        if (current.isAnalyzing) {
            return
        }

        _analysisState.value =
            _analysisState.value.copy(
                isAnalyzing = true,
            )

        viewModelScope.launch {
            try {
                val observation =
                    poseAnalysisEngine.analyze(
                        frame = frame,
                    )

                _analysisState.value =
                    _analysisState.value.copy(
                        framesAnalyzed =
                            _analysisState.value.framesAnalyzed + 1,
                        lastLandmarkCount =
                            observation.landmarks.size,
                        isAnalyzing = false,
                    )
            } catch (_: Throwable) {
                _analysisState.value =
                    _analysisState.value.copy(
                        isAnalyzing = false,
                    )
            }
        }
    }
}
