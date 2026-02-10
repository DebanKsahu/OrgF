package com.github.orgf.core.di

import com.github.orgf.core.ServiceState
import org.koin.dsl.module

fun getCoreKoinModule() = module {
    single<ServiceState> {
        ServiceState()
    }
}