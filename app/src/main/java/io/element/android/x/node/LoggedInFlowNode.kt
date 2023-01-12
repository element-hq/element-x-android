package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.Coil
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.x.architecture.bindings
import io.element.android.x.architecture.createNode
import io.element.android.x.core.di.DaggerComponentOwner
import io.element.android.x.di.SessionComponent
import io.element.android.x.features.preferences.PreferencesFlowNode
import io.element.android.x.features.roomlist.RoomListNode
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.core.SessionId
import io.element.android.x.matrix.ui.di.MatrixUIBindings
import kotlinx.parcelize.Parcelize

class LoggedInFlowNode(
    buildContext: BuildContext,
    val sessionId: SessionId,
    private val matrixClient: MatrixClient,
    private val onOpenBugReport: () -> Unit,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.RoomList,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<LoggedInFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
), DaggerComponentOwner {

    override val daggerComponent: Any by lazy {
        parent!!.bindings<SessionComponent.ParentBindings>().sessionComponentBuilder().client(matrixClient).build()
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                val imageLoaderFactory = bindings<MatrixUIBindings>().loggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
                matrixClient.startSync()
            },
            onDestroy = {
                val imageLoaderFactory = bindings<MatrixUIBindings>().notLoggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
            }
        )
    }

    private val roomListCallback = object : RoomListNode.Callback {
        override fun onRoomClicked(roomId: RoomId) {
            backstack.push(NavTarget.Room(roomId))
        }

        override fun onSettingsClicked() {
            backstack.push(NavTarget.Settings)
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object RoomList : NavTarget

        @Parcelize
        data class Room(val roomId: RoomId) : NavTarget

        @Parcelize
        object Settings : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomList -> {
                createNode<RoomListNode>(buildContext, plugins = listOf(roomListCallback))
            }
            is NavTarget.Room -> {
                RoomFlowNode(buildContext, navTarget.roomId)
            }
            NavTarget.Settings -> {
                PreferencesFlowNode(buildContext, onOpenBugReport)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backstack)
    }
}
