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

import android.app.Activity
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.oidc.CustomTabAvailabilityChecker
import io.element.android.features.login.impl.oidc.customtab.CustomTabHandler
import io.element.android.features.login.impl.oidc.webview.OidcNode
import io.element.android.features.login.impl.screens.changeaccountprovider.ChangeAccountProviderNode
import io.element.android.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderNode
import io.element.android.features.login.impl.screens.loginpassword.LoginPasswordNode
import io.element.android.features.login.impl.screens.searchaccountprovider.SearchAccountProviderNode
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class LoginFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val customTabAvailabilityChecker: CustomTabAvailabilityChecker,
    private val customTabHandler: CustomTabHandler,
    private val accountProviderDataSource: AccountProviderDataSource,
) : BackstackNode<LoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.ConfirmAccountProvider,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    private var activity: Activity? = null
    private var darkTheme: Boolean = false

    data class Inputs(
        val isAccountCreation: Boolean,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object ConfirmAccountProvider : NavTarget

        @Parcelize
        object ChangeAccountProvider : NavTarget

        @Parcelize
        object SearchAccountProvider : NavTarget

        @Parcelize
        object LoginPassword : NavTarget

        @Parcelize
        data class OidcView(val oidcDetails: OidcDetails) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.ConfirmAccountProvider -> {
                val inputs = ConfirmAccountProviderNode.Inputs(
                    isAccountCreation = inputs.isAccountCreation
                )
                val callback = object : ConfirmAccountProviderNode.Callback {
                    override fun onOidcDetails(oidcDetails: OidcDetails) {
                        if (customTabAvailabilityChecker.supportCustomTab()) {
                            // In this case open a Chrome Custom tab
                            activity?.let { customTabHandler.open(it, darkTheme, oidcDetails.url) }
                        } else {
                            // Fallback to WebView mode
                            backstack.push(NavTarget.OidcView(oidcDetails))
                        }
                    }

                    override fun onLoginPasswordNeeded() {
                        backstack.push(NavTarget.LoginPassword)
                    }

                    override fun onChangeAccountProvider() {
                        backstack.push(NavTarget.ChangeAccountProvider)
                    }
                }
                createNode<ConfirmAccountProviderNode>(buildContext, plugins = listOf(inputs, callback))
            }
            NavTarget.ChangeAccountProvider -> {
                val callback = object : ChangeAccountProviderNode.Callback {
                    override fun onDone() {
                        // Go back to the Account Provider screen
                        backstack.singleTop(NavTarget.ConfirmAccountProvider)
                    }

                    override fun onOtherClicked() {
                        backstack.push(NavTarget.SearchAccountProvider)
                    }
                }

                createNode<ChangeAccountProviderNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.SearchAccountProvider -> {
                val callback = object : SearchAccountProviderNode.Callback {
                    override fun onDone() {
                        // Go back to the Account Provider screen
                        backstack.singleTop(NavTarget.ConfirmAccountProvider)
                    }
                }

                createNode<SearchAccountProviderNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.LoginPassword -> {
                createNode<LoginPasswordNode>(buildContext, plugins = listOf())
            }
            is NavTarget.OidcView -> {
                val input = OidcNode.Inputs(navTarget.oidcDetails)
                createNode<OidcNode>(buildContext, plugins = listOf(input))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        activity = LocalContext.current as? Activity
        darkTheme = !ElementTheme.colors.isLight
        DisposableEffect(Unit) {
            onDispose {
                activity = null
                accountProviderDataSource.reset()
            }
        }
        Children(
            navModel = backstack,
            modifier = modifier,
            // Animate transition to change server screen
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
