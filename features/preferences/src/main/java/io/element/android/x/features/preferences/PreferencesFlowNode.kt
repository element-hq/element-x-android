package io.element.android.x.features.preferences

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import io.element.android.x.architecture.createNode
import io.element.android.x.features.preferences.root.PreferencesRootNode
import kotlinx.parcelize.Parcelize

class PreferencesFlowNode(
    buildContext: BuildContext,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<PreferencesFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
) {

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Root : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> createNode<PreferencesRootNode>(buildContext)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backstack)
    }
}
