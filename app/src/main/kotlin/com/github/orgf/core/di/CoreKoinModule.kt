package com.github.orgf.core.di

import com.github.orgf.core.ServiceState
import com.github.orgf.core.agent.tool.PdfTextExtractor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun getCoreKoinModule() = module {

    single<ServiceState> {
        ServiceState()
    }

    // Agent:Tools
    single<PdfTextExtractor> {
        PdfTextExtractor(platformContext = get())
    }
}