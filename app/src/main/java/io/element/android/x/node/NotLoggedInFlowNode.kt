package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.replace
import io.element.android.x.core.di.viewModelSupportNode
import io.element.android.x.features.login.node.LoginFlowNode
import io.element.android.x.features.onboarding.OnBoardingScreen
import kotlinx.parcelize.Parcelize

class NotLoggedInFlowNode(
    buildContext: BuildContext,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.OnBoarding,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<NotLoggedInFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
) {

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object OnBoarding : NavTarget

        @Parcelize
        object LoginFlow : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.OnBoarding -> viewModelSupportNode(buildContext) {
                OnBoardingScreen(
                    onSignIn = { backstack.replace(NavTarget.LoginFlow) }
                )
            }
            NavTarget.LoginFlow -> LoginFlowNode(buildContext)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = backstack)
    }
}
