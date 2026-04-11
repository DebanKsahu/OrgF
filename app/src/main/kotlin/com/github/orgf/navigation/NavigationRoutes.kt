package com.github.orgf.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer

@Serializable
sealed interface NavigationRoutes : NavKey {

    @Serializable
    data object FolderPickerScreen : NavigationRoutes

    @Serializable
    data object AppScreen : NavigationRoutes {

        @Serializable
        data object HomeScreen : NavigationRoutes

        @Serializable
        data object PromptScreen : NavigationRoutes {

            @Serializable
            data object AddPromptScreen : NavigationRoutes
        }

        @Serializable
        data object SettingsScreen : NavigationRoutes
    }
}

@OptIn(InternalSerializationApi::class)
val navigationRoutesSerializableConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(
                NavigationRoutes.FolderPickerScreen::class,
                NavigationRoutes.FolderPickerScreen::class.serializer()
            )
            subclass(
                NavigationRoutes.AppScreen::class,
                NavigationRoutes.AppScreen::class.serializer()
            )
            subclass(
                NavigationRoutes.AppScreen.HomeScreen::class,
                NavigationRoutes.AppScreen.HomeScreen::class.serializer()
            )
            subclass(
                NavigationRoutes.AppScreen.PromptScreen::class,
                NavigationRoutes.AppScreen.PromptScreen::class.serializer()
            )
            subclass(
                NavigationRoutes.AppScreen.PromptScreen.AddPromptScreen::class,
                NavigationRoutes.AppScreen.PromptScreen.AddPromptScreen::class.serializer()
            )
            subclass(
                NavigationRoutes.AppScreen.SettingsScreen::class,
                NavigationRoutes.AppScreen.SettingsScreen::class.serializer()
            )
        }
    }
}