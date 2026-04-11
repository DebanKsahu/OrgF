package com.github.orgf.promptscreen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.orgf.promptscreen.domain.model.toPromptCardUiStateList
import com.github.orgf.promptscreen.domain.repository.PromptScreenRepository
import com.github.orgf.promptscreen.ui.state.PromptScreenUiState
import com.github.orgf.utils.enums.PromptCategory
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
        loadAllPrompt()
    }

    fun loadAllPrompt() {
        viewModelScope.launch {
            _promptScreenUiState.update { oldState ->
                oldState.copy(isLoading = true, error = null)
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
                    oldState.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun loadPromptByCategory(filter: String) {
        val promptCategory = PromptCategory.valueOf(filter)
        viewModelScope.launch {
            _promptScreenUiState.update { oldState ->
                oldState.copy(isLoading = true, error = null)
            }
            try {
                val promptList =
                    promptScreenRepository.getPromptsByCategory(category = promptCategory)
                _promptScreenUiState.update { oldState ->
                    oldState.copy(
                        promptList = promptList.toPromptCardUiStateList(),
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _promptScreenUiState.update { oldState ->
                    oldState.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun updatePromptActiveStatus(promptId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            promptScreenRepository.updatePromptActiveStatus(promptId, isEnabled)
        }
    }

}