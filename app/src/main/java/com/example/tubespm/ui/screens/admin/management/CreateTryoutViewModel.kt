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
    val isSavedSuccess: Boolean = false,
    val createdTryoutId: String? = null
)

class CreateTryoutViewModel @Inject constructor(
    private val repository: ExerciseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTryoutUiState())
    val uiState: StateFlow<CreateTryoutUiState> = _uiState.asStateFlow()

    fun updateCode(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

//    fun updateStatus(status: String) {
//        _uiState.update { it.copy(status = status) }
//    }

    fun createTryout(
        onSuccess: (String) -> Unit, // Callback dengan tryoutId
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

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val newTryout = Tryout(
                    id = "", // Akan di-generate oleh repository
                    code = currentState.code,
                    title = currentState.title,
                    status = currentState.status,
                    sections = emptyList() // Awal kosong, akan diisi setelah section dibuat
                )

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