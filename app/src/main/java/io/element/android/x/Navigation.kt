package io.element.android.x

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import io.element.android.x.destinations.LoginScreenNavigationDestination
import io.element.android.x.destinations.MessagesScreenNavigationDestination
import io.element.android.x.destinations.RoomListScreenNavigationDestination
import io.element.android.x.features.login.LoginScreen
import io.element.android.x.features.messages.MessagesScreen
import io.element.android.x.features.roomlist.RoomListScreen
import io.element.android.x.matrix.core.RoomId

@Destination
@Composable
fun LoginScreenNavigation(navigator: DestinationsNavigator) {
    LoginScreen(
        onLoginWithSuccess = {
            navigator.navigate(RoomListScreenNavigationDestination){
                popUpTo(LoginScreenNavigationDestination){
                    inclusive = true
                }
            }
        }
    )
}

@RootNavGraph(start = true)
@Destination
@Composable
fun RoomListScreenNavigation(navigator: DestinationsNavigator) {
    RoomListScreen(
        onRoomClicked = { roomId: RoomId ->
            navigator.navigate(MessagesScreenNavigationDestination(roomId = roomId.value))
        },
        onSuccessLogout = {
            navigator.navigate(LoginScreenNavigationDestination){
                popUpTo(RoomListScreenNavigationDestination){
                    inclusive = true
                }
            }
        })
}

@Destination
@Composable
fun MessagesScreenNavigation(roomId: String) {
    MessagesScreen(roomId)
}



