package io.element.android.x.features.login

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.x.architecture.createNode
import io.element.android.x.features.login.changeserver.ChangeServerNode
import io.element.android.x.features.login.root.LoginRootNode
import kotlinx.parcelize.Parcelize

class LoginFlowNode(
    buildContext: BuildContext,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<LoginFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
) {

    private val loginRootCallback = object : LoginRootNode.Callback {
        override fun onChangeHomeServer() {
            backstack.push(NavTarget.ChangeServer)
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Root : NavTarget

        @Parcelize
        object ChangeServer : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> createNode<LoginRootNode>(buildContext, plugins = listOf(loginRootCallback))
            NavTarget.ChangeServer -> createNode<ChangeServerNode>(buildContext)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backstack)
    }
}
