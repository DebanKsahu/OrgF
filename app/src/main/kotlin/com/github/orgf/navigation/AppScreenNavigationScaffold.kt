package com.github.orgf.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.github.orgf.promptscreen.ui.PromptScreenUi
import com.github.orgf.utils.ui.NavigationAccentCyan
import com.github.orgf.utils.ui.NavigationBackgroundBlue
import com.github.orgf.utils.ui.NavigationInactiveBlue

@Composable
fun AppScreenNavigationScaffold(
    modifier: Modifier = Modifier
) {

    val navigationState = rememberNavigationState(
        startRoute = NavigationRoutes.AppScreen.HomeScreen,
        topLevelRoutes = appScreenNavigationDestinations.keys
    )
    val navigator = AppScreenNavigator(navigationState = navigationState)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            appScreenNavigationDestinations.forEach { (route, destination) ->
                val isSelected = navigationState.topLevelRoute == route
                item(
                    selected = isSelected,
                    onClick = {
                        navigator.navigate(route)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = destination.icon),
                            contentDescription = destination.contentDescription,
                            tint = if (isSelected) NavigationAccentCyan else NavigationInactiveBlue
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            color = if (isSelected) NavigationAccentCyan else NavigationInactiveBlue
                        )
                    }
                )
            }
        },
        modifier = modifier,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = NavigationBackgroundBlue,
            shortNavigationBarContainerColor = NavigationBackgroundBlue,
            navigationRailContainerColor = NavigationBackgroundBlue,
            navigationDrawerContainerColor = NavigationBackgroundBlue,
            navigationBarContentColor = NavigationInactiveBlue,
            shortNavigationBarContentColor = NavigationInactiveBlue,
            navigationRailContentColor = NavigationInactiveBlue,
            navigationDrawerContentColor = NavigationInactiveBlue,
        ),
    ) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
            onBack = {
                navigator.navigateBack()
            },
            entries = navigationState.toNavEntries(
                entryProvider = entryProvider {
                    entry<NavigationRoutes.AppScreen.HomeScreen> {
                        Text("Home Screen")
                    }
                    entry<NavigationRoutes.AppScreen.PromptScreen> {
                        PromptScreenUi()
                    }
                    entry<NavigationRoutes.AppScreen.SettingsScreen> {
                        Text("Settings Screen")
                    }
                }
            )
        )
    }
}