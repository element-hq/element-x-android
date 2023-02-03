/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.features.login.LoginFlowNode
import io.element.android.features.onboarding.OnBoardingScreen
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import kotlinx.parcelize.Parcelize
import timber.log.Timber

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

    init {
        lifecycle.subscribe(
            onCreate = { Timber.v("OnCreate") },
            onDestroy = { Timber.v("OnDestroy") }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object OnBoarding : NavTarget

        @Parcelize
        object LoginFlow : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.OnBoarding -> node(buildContext) {
                OnBoardingScreen(
                    onSignIn = { backstack.push(NavTarget.LoginFlow) }
                )
            }
            NavTarget.LoginFlow -> LoginFlowNode(buildContext)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            // Animate navigation to login screen
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
