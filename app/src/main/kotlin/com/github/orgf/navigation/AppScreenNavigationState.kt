package com.github.orgf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import kotlinx.serialization.PolymorphicSerializer

class AppScreenNavigationState(
    val startRoute: NavKey,
    val topLevelRoute: MutableState<NavKey>,
    val backStackMap: Map<NavKey, NavBackStack<NavKey>>
) {
    val stackInUse: List<NavKey>
        get() = if (topLevelRoute.value == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute.value)
        }
}

@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>
): AppScreenNavigationState {
    val currTopLevelRoute = rememberSerializable(
        startRoute,
        topLevelRoutes,
        configuration = navigationRoutesSerializableConfig,
        serializer = MutableStateSerializer(PolymorphicSerializer(NavKey::class))
    ) {
        mutableStateOf(startRoute)
    }

    val backStackMap = topLevelRoutes.associateWith { navKey ->
        rememberNavBackStack(
            configuration = navigationRoutesSerializableConfig,
            navKey
        )
    }

    val navigationState = remember(startRoute, topLevelRoutes) {
        AppScreenNavigationState(
            startRoute = startRoute,
            topLevelRoute = currTopLevelRoute,
            backStackMap = backStackMap
        )
    }

    return navigationState
}

@Composable
fun AppScreenNavigationState.toNavEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedNavEntries = backStackMap.mapValues { (navKey, backStack) ->
        rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider
        )
    }

    return stackInUse.flatMap { navKey -> decoratedNavEntries[navKey] ?: emptyList() }
        .toMutableStateList()
}