package com.github.orgf.di

import com.github.orgf.core.di.getCoreKoinModule
import com.github.orgf.folderpickerscreen.di.getFolderPickerKoinModule
import com.github.orgf.promptscreen.di.getPromptScreenKoinModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    platformDeclaration: KoinAppDeclaration = {}
) {
    startKoin {
        platformDeclaration()
        modules(
            // Core
            getCoreKoinModule(),

            // Folder Picker
            getFolderPickerKoinModule(),

            // Prompt Screen
            getPromptScreenKoinModule()
        )


    }
}