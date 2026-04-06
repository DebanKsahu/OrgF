package com.github.orgf.promptscreen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.orgf.promptscreen.domain.model.PromptDetailDomain
import com.github.orgf.promptscreen.domain.repository.PromptScreenRepository
import com.github.orgf.promptscreen.ui.state.NewPromptUiState
import com.github.orgf.utils.enums.PromptCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPromptScreenViewModel(
    private val promptScreenRepository: PromptScreenRepository,
) : ViewModel() {

    private val _newPromptState = MutableStateFlow<NewPromptUiState>(NewPromptUiState())
    val newPromptState = _newPromptState.asStateFlow()

    fun addPrompt() {
        viewModelScope.launch {
            promptScreenRepository.addPrompt(
                promptDetail = PromptDetailDomain(
                    prompt = _newPromptState.value.prompt,
                    category = _newPromptState.value.category,
                    destinationFolder = _newPromptState.value.destinationFolder
                )
            )
        }
    }

    fun updatePromptCategory(promptCategory: PromptCategory) {
        _newPromptState.update { oldState ->
            oldState.copy(category = promptCategory)
        }
    }

    fun updatePrompt(prompt: String) {
        _newPromptState.update { oldState ->
            oldState.copy(prompt = prompt)
        }
    }

    fun updateDestinationFolder(destinationFolder: String) {
        _newPromptState.update { oldState ->
            oldState.copy(destinationFolder = destinationFolder)
        }
    }
}