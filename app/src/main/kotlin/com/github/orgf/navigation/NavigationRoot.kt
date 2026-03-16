package com.github.orgf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.github.orgf.folderpickerscreen.ui.FolderPickerUi

@Composable
fun NavigationRoot() {

    val initialRoute = NavigationRoutes.FolderPickerScreen

    val rootNavigationBackstack = rememberNavBackStack(
        configuration = navigationRoutesSerializableConfig,
        initialRoute
    )

    NavDisplay(
        modifier = Modifier,
        backStack = rootNavigationBackstack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<NavigationRoutes.FolderPickerScreen> {
                FolderPickerUi(
                    onSuccessfulWorkspaceSelection = {
                        rootNavigationBackstack.add(NavigationRoutes.AppScreen)
                        rootNavigationBackstack.remove(NavigationRoutes.FolderPickerScreen)
                    }
                )
            }
            entry<NavigationRoutes.AppScreen> {
                AppScreenNavigationScaffold()
            }
        }
    )

}