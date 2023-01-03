package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.x.core.di.viewModelSupportNode
import io.element.android.x.features.messages.MessagesScreen
import io.element.android.x.features.roomlist.RoomListNode
import io.element.android.x.features.roomlist.RoomListPresenter
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.core.SessionId
import kotlinx.parcelize.Parcelize
import timber.log.Timber

class LoggedInFlowNode(
    buildContext: BuildContext,
    val sessionId: SessionId,
    private val matrixClient: MatrixClient,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.RoomList,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<LoggedInFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
) {

    init {
        lifecycle.subscribe(
            onCreate = { Timber.v("OnCreate") },
            onDestroy = { Timber.v("OnDestroy") }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object RoomList : NavTarget

        @Parcelize
        data class Messages(val roomId: RoomId) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomList -> RoomListNode(
                buildContext = buildContext,
                presenter = RoomListPresenter(matrixClient),
                onRoomClicked = {
                    backstack.push(NavTarget.Messages(it))
                })
            is NavTarget.Messages -> viewModelSupportNode(buildContext) {
                MessagesScreen(
                    roomId = navTarget.roomId.value,
                    onBackPressed = { backstack.pop() }
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backstack)
    }
}
