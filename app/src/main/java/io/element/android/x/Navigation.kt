package io.element.android.x

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.element.android.x.destinations.LoginScreenNavigationDestination
import io.element.android.x.destinations.RoomListScreenNavigationDestination
import io.element.android.x.features.login.LoginScreen
import io.element.android.x.features.roomlist.RoomListScreen

@Destination
@Composable
fun LoginScreenNavigation(navigator: DestinationsNavigator) {
    LoginScreen(
        onLoginWithSuccess = {
            navigator.clearBackStack(RoomListScreenNavigationDestination)
        }
    )
}

@RootNavGraph(start = true)
@Destination
@Composable
fun RoomListScreenNavigation(navigator: DestinationsNavigator) {
    RoomListScreen(onSuccessLogout = {
        navigator.clearBackStack(LoginScreenNavigationDestination)
    })
}


