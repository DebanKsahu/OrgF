package com.github.orgf.core.di

import androidx.room.Room
import com.github.orgf.core.ServiceState
import com.github.orgf.core.agent.LlmInferences
import com.github.orgf.core.agent.prompt.PromptManager
import com.github.orgf.core.agent.tool.PdfTextExtractor
import com.github.orgf.core.agent.tool.TextEmbedding
import com.github.orgf.core.database.AppDatabase
import com.github.orgf.core.database.dao.PromptTableDao
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

fun getCoreKoinModule() = module {

    single<ServiceState> {
        ServiceState()
    }

    // Agent:Tools
    single<PdfTextExtractor> {
        PdfTextExtractor(platformContext = get())
    }
    factory<TextEmbedding> { (currentModel: Int, currentDelegate: Int) ->
        TextEmbedding(
            platformContext = get(),
            currentModel = currentModel,
            currentDelegate = currentDelegate
        )
    }

    // Agent:Prompt
    factory<PromptManager> {
        PromptManager(
            appDatabase = get(),
            textEmbeddingTools = get(
                parameters = {
                    parametersOf(
                        TextEmbedding.UNIVERSAL_SENTENCE_ENCODER,
                        TextEmbedding.DELEGATE_CPU
                    )
                }
            )
        )
    }

    // Agent:LLM
    single<LlmInferences> {
        LlmInferences()
    }

    // Database:Room
    single<AppDatabase> {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "orgf_database")
            .build()
    }

    single<PromptTableDao> {
        get<AppDatabase>().promptTableDao()
    }
}