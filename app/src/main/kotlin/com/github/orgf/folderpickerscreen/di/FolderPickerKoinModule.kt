package com.github.orgf.folderpickerscreen.di

import com.github.orgf.folderpickerscreen.ui.FolderPickerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun getFolderPickerKoinModule() = module {
    viewModel<FolderPickerViewModel> {
        FolderPickerViewModel()
    }
}