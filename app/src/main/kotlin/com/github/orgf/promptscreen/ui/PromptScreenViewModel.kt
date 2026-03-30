package com.github.orgf.promptscreen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.orgf.promptscreen.domain.repository.PromptScreenRepository
import com.github.orgf.promptscreen.ui.mapper.toPromptCardUiStateList
import com.github.orgf.promptscreen.ui.state.PromptScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PromptScreenViewModel(
    private val promptScreenRepository: PromptScreenRepository,
) : ViewModel() {

    private val _promptScreenUiState = MutableStateFlow(PromptScreenUiState())
    val promptScreenUiState = _promptScreenUiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadAllPrompt()
        }
    }

    suspend fun loadAllPrompt() {
        _promptScreenUiState.update { oldState ->
            oldState.copy(promptList = null, isLoading = true, error = null)
        }
        try {
            val promptList = promptScreenRepository.getAllPrompts()
            _promptScreenUiState.update { oldState ->
                oldState.copy(
                    promptList = promptList.toPromptCardUiStateList(),
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            _promptScreenUiState.update { oldState ->
                oldState.copy(promptList = null, isLoading = false, error = e.message)
            }
        }
    }

}