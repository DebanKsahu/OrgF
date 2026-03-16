package com.github.orgf.navigation

import com.github.orgf.R

enum class AppScreenNavigationDestination(
    val label: String, val icon: Int, val contentDescription: String
) {
    HOME(
        label = "Home",
        icon = R.drawable.home_icon,
        contentDescription = "A beautiful home icon"
    ),
    PROMPT(
        label = "Prompt",
        icon = R.drawable.prompt_icon,
        contentDescription = "A beautiful prompt icon"
    ),
    SETTINGS(
        label = "Settings",
        icon = R.drawable.settings_icon,
        contentDescription = "A beautiful settings icon"
    )
}

val appScreenNavigationDestinations = mapOf(
    NavigationRoutes.AppScreen.HomeScreen to AppScreenNavigationDestination.HOME,
    NavigationRoutes.AppScreen.PromptScreen to AppScreenNavigationDestination.PROMPT,
    NavigationRoutes.AppScreen.SettingsScreen to AppScreenNavigationDestination.SETTINGS,
)