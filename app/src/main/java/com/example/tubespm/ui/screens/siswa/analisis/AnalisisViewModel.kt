package com.example.tubespm.ui.screens.siswa.analisis

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.data.model.UserActivity
import com.example.tubespm.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubtestScoreDetail(
    val id: String,
    val name: String, // Nama cantik (Penalaran Umum)
    val score: Int
)

data class AnalisisUiState(
    val activityId: String = "",
    val isLoading: Boolean = true,
    val totalScore: Int = 0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0, // Dari metadata tryout
    val scoreDetails: List<SubtestScoreDetail> = emptyList()
)

@HiltViewModel
class AnalisisViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val activityId: String = savedStateHandle.get<String>("activityId")!!
    private val _uiState = MutableStateFlow(AnalisisUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAnalysis()
    }

    private fun loadAnalysis() {
        viewModelScope.launch {
            val activity = repository.getActivity(activityId)
            if (activity != null) {
                // Ambil Metadata Tryout untuk mendapatkan Nama Subtest yang asli
                val tryoutMetadata = repository.getQuizMetadata(activity.activityRefId, activity.type) as? Tryout

                // Buat Peta Nama Subtest (ID -> Nama Panjang)
                val subtestNameMap = mutableMapOf<String, String>()
                tryoutMetadata?.sections?.forEach { section ->
                    section.subtests.forEach { subtest ->
                        subtestNameMap[subtest.subtestId] = subtest.subtestName
                    }
                }
                // Fallback untuk Latihan Soal (yang subtestnya di root)
                if (activity.type == "latihan_soal") {
                    // Latihan soal biasanya cuma 1 subtest, namanya ada di Title/Subtest field
                    // Tapi karena struktur map 'subtestScores' mungkin kosong atau custom, kita handle generic
                }

                // Mapping data skor
                val details = activity.subtestScores.map { (id, score) ->
                    SubtestScoreDetail(
                        id = id,
                        name = subtestNameMap[id] ?: id.uppercase(), // Gunakan ID jika nama tidak ketemu
                        score = score
                    )
                }

                _uiState.update {
                    it.copy(
                        activityId = activityId,
                        isLoading = false,
                        totalScore = activity.score,
                        correctCount = activity.correctCount,
                        totalQuestions = tryoutMetadata?.totalQuestionCount ?: 0,
                        scoreDetails = details
                    )
                }
            }
        }
    }
}