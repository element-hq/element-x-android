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
