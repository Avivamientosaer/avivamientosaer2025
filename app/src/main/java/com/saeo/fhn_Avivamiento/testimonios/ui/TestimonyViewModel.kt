package com.saeo.fhn_Avivamiento.testimonios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saeo.fhn_Avivamiento.data.local.AppDatabase
import com.saeo.fhn_Avivamiento.testimonios.data.Testimony
import com.saeo.fhn_Avivamiento.testimonios.data.TestimonyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TestimonyUiState(
    val testimonies: List<Testimony> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentTestimony: Testimony? = null
)

class TestimonyViewModel(
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestimonyUiState())
    val uiState: StateFlow<TestimonyUiState> = _uiState.asStateFlow()

    init {
        loadTestimonies()
    }

    fun loadTestimonies() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val testimonies = database.testimonyDao().getAllActiveTestimonies()
                _uiState.update { it.copy(testimonies = testimonies, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Error cargando testimonios: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun saveTestimony(testimony: Testimony) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                database.testimonyDao().insertTestimony(testimony)
                loadTestimonies() // Refresh la lista
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Error guardando testimonio: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun deleteTestimony(testimonyId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                database.testimonyDao().markAsDeleted(testimonyId)
                loadTestimonies() // Refresh la lista
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Error eliminando testimonio: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setCurrentTestimony(testimony: Testimony?) {
        _uiState.update { it.copy(currentTestimony = testimony) }
    }

    fun updateTestimonyField(
        countryCode: String = _uiState.value.currentTestimony?.countryCode ?: "",
        phone: String = _uiState.value.currentTestimony?.phone ?: "",
        name: String = _uiState.value.currentTestimony?.name ?: "",
        chapter: String = _uiState.value.currentTestimony?.chapter ?: "",
        city: String = _uiState.value.currentTestimony?.city ?: "",
        testimonyType: TestimonyType = _uiState.value.currentTestimony?.testimonyType ?: TestimonyType.INDIVIDUAL,
        joinedYear: Int = _uiState.value.currentTestimony?.joinedYear ?: 0,
        birthYear: Int = _uiState.value.currentTestimony?.birthYear ?: 0,
        notes: String = _uiState.value.currentTestimony?.notes ?: ""
    ) {
        _uiState.update { state ->
            state.copy(
                currentTestimony = state.currentTestimony?.copy(
                    countryCode = countryCode,
                    phone = phone,
                    name = name,
                    chapter = chapter,
                    city = city,
                    testimonyType = testimonyType,
                    joinedYear = joinedYear,
                    birthYear = birthYear,
                    notes = notes
                ) ?: Testimony(
                    countryCode = countryCode,
                    phone = phone,
                    name = name,
                    chapter = chapter,
                    city = city,
                    testimonyType = testimonyType,
                    joinedYear = joinedYear,
                    birthYear = birthYear,
                    notes = notes
                )
            )
        }
    }
}