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
    val status: String = "inactive",
    val topics: List<Topic> = emptyList(),
    val error: String? = null,
    val isSavedSuccess: Boolean = false,
    val createdLatihanId: String? = null,
    val topicsString: String = "",
    // --- TAMBAH STATE UNTUK VALIDASI DUPLIKASI ---
    val codeDuplicateError: String? = null,
    val titleDuplicateError: String? = null
)

class CreateLatihanSoalViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateLatihanSoalUiState())
    val uiState: StateFlow<CreateLatihanSoalUiState> = _uiState.asStateFlow()

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
        val id = subtestMap[subtestName] ?: "umum"
        _uiState.update { it.copy(subtest = subtestName, subtestId = id) }
    }

    fun updateCode(code: String) {
        _uiState.update { it.copy(code = code, codeDuplicateError = null) } // <--- Reset error
        // Tambahkan pengecekan duplikasi saat input berubah
        if (code.isNotBlank()) {
            checkCodeDuplication(code)
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleDuplicateError = null) } // <--- Reset error
        // Tambahkan pengecekan duplikasi saat input berubah
        if (title.isNotBlank()) {
            checkTitleDuplication(title)
        }
    }

    fun updateTopicsString(text: String) {
        _uiState.update { it.copy(topicsString = text) }
    }

    // --- FUNGSI BARU UNTUK CEK DUPLIKASI ---

    private fun checkCodeDuplication(code: String) {
        viewModelScope.launch {
            try {
                if (repository.isLatihanSoalCodeDuplicate(code)) {
                    _uiState.update {
                        it.copy(codeDuplicateError = "Kode '$code' sudah ada")
                    }
                } else {
                    _uiState.update { it.copy(codeDuplicateError = null) }
                }
            } catch (e: Exception) {
                // Handle error pengecekan, misal tidak perlu error UI, cukup log
                println("Error checking code duplication: ${e.message}")
            }
        }
    }

    private fun checkTitleDuplication(title: String) {
        viewModelScope.launch {
            try {
                if (repository.isLatihanSoalTitleDuplicate(title)) {
                    _uiState.update {
                        it.copy(titleDuplicateError = "Judul '$title' sudah ada")
                    }
                } else {
                    _uiState.update { it.copy(titleDuplicateError = null) }
                }
            } catch (e: Exception) {
                println("Error checking title duplication: ${e.message}")
            }
        }
    }


    fun createLatihanSoal(
        onSuccess: (String) -> Unit, // Callback dengan latihanId
        onError: (String) -> Unit
    ) {
        val currentState = _uiState.value

        // Validasi Awal
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

        // Validasi Duplikasi State
        if (currentState.codeDuplicateError != null) {
            onError(currentState.codeDuplicateError)
            return
        }
        if (currentState.titleDuplicateError != null) {
            onError(currentState.titleDuplicateError)
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {

                // DOUBLE CHECK DUPLIKASI SEBELUM SAVE (PENTING!)
                // Kasus: User mengetik, error muncul, lalu dia cepat-cepat klik save
                val isCodeDuplicateFinal = repository.isLatihanSoalCodeDuplicate(currentState.code)
                val isTitleDuplicateFinal = repository.isLatihanSoalTitleDuplicate(currentState.title)

                if (isCodeDuplicateFinal) {
                    _uiState.update {
                        it.copy(isSaving = false, codeDuplicateError = "Kode '$${currentState.code}' sudah ada")
                    }
                    onError(_uiState.value.codeDuplicateError ?: "Kode sudah ada")
                    return@launch
                }

                if (isTitleDuplicateFinal) {
                    _uiState.update {
                        it.copy(isSaving = false, titleDuplicateError = "Judul '$${currentState.title}' sudah ada")
                    }
                    onError(_uiState.value.titleDuplicateError ?: "Judul sudah ada")
                    return@launch
                }

                val topicList = currentState.topicsString.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { name ->
                        Topic(topicId = name.lowercase().replace(" ", "_"), name = name)
                    }

                val mappedSubtestId = subtestMap[currentState.subtest] ?: "umum"

                val newLatihan = LatihanSoal(
                    id = "",
                    code = currentState.code,
                    title = currentState.title,
                    subtest = currentState.subtest,
                    subtestId = mappedSubtestId,
                    questionCount = 0,
                    status = currentState.status,
                    topics = topicList
                    // codeLower dan titleLower akan diisi di repository
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