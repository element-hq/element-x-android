package io.element.android.x

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import io.element.android.x.destinations.*
import io.element.android.x.features.login.LoginScreen
import io.element.android.x.features.login.changeserver.ChangeServerScreen
import io.element.android.x.features.messages.MessagesScreen
import io.element.android.x.features.onboarding.OnBoardingScreen
import io.element.android.x.features.roomlist.RoomListScreen
import io.element.android.x.matrix.core.RoomId

@Destination
@Composable
fun OnBoardingScreenNavigation(navigator: DestinationsNavigator) {
    OnBoardingScreen(
        onSignUp = {
            // TODO
        },
        onSignIn = {
            navigator.navigate(LoginScreenNavigationDestination)
        }
    )
}

@Destination
@Composable
fun LoginScreenNavigation(navigator: DestinationsNavigator) {
    LoginScreen(
        onChangeServer = {
            navigator.navigate(ChangeServerScreenNavigationDestination)
        },
        onLoginWithSuccess = {
            navigator.navigate(RoomListScreenNavigationDestination) {
                popUpTo(OnBoardingScreenNavigationDestination) {
                    inclusive = true
                }
            }
        }
    )
}

// TODO Create a subgraph in Login module
@Destination
@Composable
fun ChangeServerScreenNavigation(navigator: DestinationsNavigator) {
    ChangeServerScreen(
        onChangeServerSuccess = {
            navigator.popBackStack()
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
            navigator.navigate(OnBoardingScreenNavigationDestination) {
                popUpTo(RoomListScreenNavigationDestination) {
                    inclusive = true
                }
            }
        }
    )
}

@Destination
@Composable
fun MessagesScreenNavigation(roomId: String, navigator: DestinationsNavigator) {
    MessagesScreen(roomId, navigator::navigateUp)
}
