package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.LatihanSoal
import com.example.tubespm.data.model.Topic
import com.example.tubespm.repository.ExerciseCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateLatihanSoalUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val code: String = "",
    val title: String = "",
    val subtest: String = "",
    val subtestId: String = "",
    val status: String = "active",
    val topics: List<Topic> = emptyList(), // Ini untuk hasil jadi (opsional di UI state create)
    val error: String? = null,
    val isSavedSuccess: Boolean = false,
    val createdLatihanId: String? = null,
    val topicsString: String = ""
)

class CreateLatihanSoalViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateLatihanSoalUiState())
    val uiState: StateFlow<CreateLatihanSoalUiState> = _uiState.asStateFlow()

    // Map Nama Subtest -> ID
    private val subtestMap = mapOf(
        "Penalaran Umum" to "pu",
        "Pengetahuan Kuantitatif" to "pk",
        "Pengetahuan dan Pemahaman Umum" to "ppu",
        "Pemahaman Bacaan dan Menulis" to "pbm",
        "Literasi dalam Bahasa Indonesia" to "lbi",
        "Literasi dalam Bahasa Inggris" to "lbing",
        "Penalaran Matematika" to "pm"
    )

    fun updateSubtest(subtestName: String) {
        // Otomatis cari ID berdasarkan nama
        val id = subtestMap[subtestName] ?: "umum"
        _uiState.update { it.copy(subtest = subtestName, subtestId = id) }
    }

    fun updateCode(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateStatus(status: String) {
        _uiState.update { it.copy(status = status) }
    }

    fun updateTopicsString(text: String) {
        _uiState.update { it.copy(topicsString = text) }
    }

    fun createLatihanSoal(
        onSuccess: (String) -> Unit, // Callback dengan latihanId
        onError: (String) -> Unit
    ) {
        val currentState = _uiState.value

        // Validasi
        if (currentState.code.isBlank()) {
            onError("Kode tidak boleh kosong")
            return
        }

        if (currentState.title.isBlank()) {
            onError("Judul tidak boleh kosong")
            return
        }

        if (currentState.subtest.isBlank()) {
            onError("Subtest tidak boleh kosong")
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {

                val topicList = currentState.topicsString.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { name ->
                        Topic(topicId = name.lowercase().replace(" ", "_"), name = name)
                    }

                // Ensure the map exists or use a fallback logic
                val mappedSubtestId = subtestMap[currentState.subtest] ?: "umum"

                val newLatihan = LatihanSoal(
                    id = "", // Akan di-generate oleh repository
                    code = currentState.code,
                    title = currentState.title,
                    subtest = currentState.subtest,
                    subtestId = mappedSubtestId,
                    questionCount = 0, // Awal 0, akan di-update setelah soal ditambahkan
                    status = currentState.status,
                    topics = topicList
                )

                val latihanId = repository.createLatihanSoal(newLatihan)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccess = true,
                        createdLatihanId = latihanId
                    )
                }

                onSuccess(latihanId)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Gagal menyimpan: ${e.message}"
                    )
                }
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateLatihanSoalUiState()
    }
}