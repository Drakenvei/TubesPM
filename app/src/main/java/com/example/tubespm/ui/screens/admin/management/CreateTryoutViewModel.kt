package com.example.tubespm.ui.screens.admin.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubespm.data.model.Tryout
import com.example.tubespm.repository.ExerciseCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTryoutUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val code: String = "",
    val title: String = "",
    val status: String = "inactive",
    val error: String? = null,
    val codeError: String? = null, // Error untuk field code
    val titleError: String? = null, // Error untuk field title
    val isSavedSuccess: Boolean = false,
    val createdTryoutId: String? = null
)

class CreateTryoutViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTryoutUiState())
    val uiState: StateFlow<CreateTryoutUiState> = _uiState.asStateFlow()

    fun updateCode(code: String) {
        // Reset error saat input diubah
        _uiState.update { it.copy(code = code, codeError = null) }
    }

    fun updateTitle(title: String) {
        // Reset error saat input diubah
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        // 1. Validasi Code (Tidak Boleh Kosong)
        if (currentState.code.isBlank()) {
            _uiState.update { it.copy(codeError = "Kode tidak boleh kosong") }
            isValid = false
        } else {
            _uiState.update { it.copy(codeError = null) }
        }

        // 2. Validasi Title (Tidak Boleh Kosong)
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Judul tidak boleh kosong") }
            isValid = false
        } else {
            // Kita tidak mereset error dari duplikasi di sini, hanya error 'isBlank'
            // Duplikasi akan direset di awal createTryout()
        }

        return isValid
    }

    fun createTryout(
        onSuccess: (String) -> Unit, // Callback dengan tryoutId
        onError: (String) -> Unit
    ) {
        // Pengecekan isBlank di awal
        if (!validateFields()) {
            // Error sudah di-update di validateFields()
            onError("Mohon lengkapi data yang wajib diisi.")
            return
        }

        val currentState = _uiState.value
        // Reset error & set saving state
        _uiState.update { it.copy(isSaving = true, error = null, codeError = null, titleError = null) }

        viewModelScope.launch {
            try {
                // Konversi ke huruf kecil untuk pengecekan duplikasi (Case-Insensitive Check)
                val codeForCheck = currentState.code.lowercase()
                val titleForCheck = currentState.title.lowercase()

                // 3. Pengecekan Duplikasi Code secara asinkron (menggunakan fungsi baru dari Repository)
                // Repository akan query ke field 'codeLowercase'
                val isCodeDuplicate = repository.isTryoutCodeDuplicate(codeForCheck) // <--- MENGIRIM LOWERCASE
                if (isCodeDuplicate) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            codeError = "Kode Tryout '${currentState.code}' sudah digunakan"
                        )
                    }
                    onError("Kode Tryout sudah digunakan.")
                    return@launch
                }

                // 4. Pengecekan Duplikasi Title secara asinkron (menggunakan fungsi baru dari Repository)
                // Repository akan query ke field 'titleLowercase'
                val isTitleDuplicate = repository.isTryoutTitleDuplicate(titleForCheck) // <--- MENGIRIM LOWERCASE
                if (isTitleDuplicate) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            titleError = "Judul Tryout '${currentState.title}' sudah ada"
                        )
                    }
                    onError("Judul Tryout sudah ada.")
                    return@launch
                }

                // Jika semua validasi lolos, simpan data
                val newTryout = Tryout(
                    id = "", // Akan di-generate oleh repository
                    code = currentState.code, // Simpan code asli (case-sensitive)
                    title = currentState.title, // Simpan title asli (case-sensitive)
                    // Status awal Tryout baru harus 'draft'
                    status = "draft",
                    sections = emptyList() // Awal kosong
                )
                // Note: codeLowercase dan titleLowercase akan diisi di layer Repository (createTryout)

                val tryoutId = repository.createTryout(newTryout)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSavedSuccess = true,
                        createdTryoutId = tryoutId
                    )
                }

                onSuccess(tryoutId)

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
        _uiState.value = CreateTryoutUiState()
    }
}