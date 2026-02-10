package com.github.orgf.di

import com.github.orgf.core.di.getCoreKoinModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(
    platformDeclaration: KoinAppDeclaration = {}
) {
    startKoin {
        platformDeclaration()
        modules(
            // Core
            getCoreKoinModule()
        )
    }
}