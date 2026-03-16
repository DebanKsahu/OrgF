package com.github.orgf.navigation

import androidx.navigation3.runtime.NavKey

class AppScreenNavigator(
    val navigationState: AppScreenNavigationState
) {
    fun navigate(route: NavKey) {
        if (route in navigationState.backStackMap.keys) {
            navigationState.topLevelRoute.value = route
        } else {
            navigationState.backStackMap[navigationState.topLevelRoute.value]?.add(route)
        }
    }

    fun navigateBack() {
        val currentBackStack = navigationState.backStackMap[navigationState.topLevelRoute.value]
            ?: error("No Backstack for route ${navigationState.topLevelRoute.value}")

        val currentRoute = currentBackStack.last()

        if (currentRoute == navigationState.topLevelRoute.value) {
            navigationState.topLevelRoute.value = navigationState.startRoute
        } else {
            currentBackStack.removeLastOrNull()
        }
    }
}