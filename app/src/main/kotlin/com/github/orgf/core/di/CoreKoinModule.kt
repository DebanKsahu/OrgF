package com.github.orgf.core.di

import androidx.room.Room
import com.github.orgf.core.ServiceState
import com.github.orgf.core.agent.LlmInferences
import com.github.orgf.core.agent.tool.PdfTextExtractor
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.core.database.dao.PromptTableDao
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

    // Agent:LLM
    single<LlmInferences> {
        LlmInferences()
    }

    // Database:Room
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "orgf_database")
            .build()
    }

    single<PromptTableDao> {
        get<AppDatabase>().promptTableDao()
    }
}