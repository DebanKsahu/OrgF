package com.github.orgf.promptscreen.di

import com.github.orgf.promptscreen.data.repository.PromptScreenRepositoryImpl
import com.github.orgf.promptscreen.domain.repository.PromptScreenRepository
import com.github.orgf.promptscreen.ui.AddPromptScreenViewModel
import com.github.orgf.promptscreen.ui.PromptScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getPromptScreenKoinModule() = module {
    viewModel<PromptScreenViewModel> {
        PromptScreenViewModel(promptScreenRepository = get())
    }

    viewModel<AddPromptScreenViewModel> {
        AddPromptScreenViewModel(promptScreenRepository = get())
    }

    factory<PromptScreenRepository> {
        PromptScreenRepositoryImpl(
            appDatabase = get(),
            promptManager = get()
        )
    }
}