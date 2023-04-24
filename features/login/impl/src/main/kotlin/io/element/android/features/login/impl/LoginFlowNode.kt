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

package io.element.android.features.login.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.changeserver.ChangeServerNode
import io.element.android.features.login.impl.oidc.CustomTabHandler
import io.element.android.features.login.impl.oidc.OidcNode
import io.element.android.features.login.impl.root.LoginRootNode
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class LoginFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val customTabHandler: CustomTabHandler,
) : BackstackNode<LoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Root : NavTarget

        @Parcelize
        object ChangeServer : NavTarget

        @Parcelize
        data class OidcView(val oidcDetails: OidcDetails) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : LoginRootNode.Callback {
                    override fun onChangeHomeServer() {
                        backstack.push(NavTarget.ChangeServer)
                    }

                    override fun onOidcDetails(oidcDetails: OidcDetails) {
                        if (customTabHandler.supportCustomTab()) {
                            customTabHandler.open(oidcDetails.url)
                        } else {
                            // Fallback to WebView mode
                            backstack.push(NavTarget.OidcView(oidcDetails))
                        }
                    }
                }
                createNode<LoginRootNode>(buildContext, plugins = listOf(callback))
            }

            NavTarget.ChangeServer -> createNode<ChangeServerNode>(buildContext)
            is NavTarget.OidcView -> {
                val input = OidcNode.Inputs(navTarget.oidcDetails)
                createNode<OidcNode>(buildContext, plugins = listOf(input))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            // Animate transition to change server screen
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
