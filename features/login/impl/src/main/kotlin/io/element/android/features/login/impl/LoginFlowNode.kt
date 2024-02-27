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
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.features.login.api.oidc.OidcActionFlow
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.oidc.CustomTabAvailabilityChecker
import io.element.android.features.login.impl.oidc.customtab.CustomTabHandler
import io.element.android.features.login.impl.oidc.webview.OidcNode
import io.element.android.features.login.impl.screens.changeaccountprovider.ChangeAccountProviderNode
import io.element.android.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderNode
import io.element.android.features.login.impl.screens.loginpassword.LoginFormState
import io.element.android.features.login.impl.screens.loginpassword.LoginPasswordNode
import io.element.android.features.login.impl.screens.searchaccountprovider.SearchAccountProviderNode
import io.element.android.features.login.impl.screens.waitlistscreen.WaitListNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class LoginFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val customTabAvailabilityChecker: CustomTabAvailabilityChecker,
    private val customTabHandler: CustomTabHandler,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val defaultLoginUserStory: DefaultLoginUserStory,
    private val oidcActionFlow: OidcActionFlow,
) : BaseFlowNode<LoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    private var activity: Activity? = null
    private var darkTheme: Boolean = false

    data class Inputs(
        val isAccountCreation: Boolean,
        val isQrCode: Boolean,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    private var customChromeTabStarted = false

    override fun onBuilt() {
        super.onBuilt()
        defaultLoginUserStory.setLoginFlowIsDone(false)
        lifecycle.subscribe(
            onResume = {
                if (customChromeTabStarted) {
                    customChromeTabStarted = false
                    // Workaround to detect that the Custom Chrome Tab has been closed
                    // If there is no coming OidcAction (that would end this Node),
                    // consider that the user has cancelled the login
                    // by pressing back or by closing the Custom Chrome Tab.
                    lifecycleScope.launch {
                        delay(5000)
                        oidcActionFlow.post(OidcAction.GoBack)
                    }
                }
            }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object QrCode : NavTarget

        @Parcelize
        data object ConfirmAccountProvider : NavTarget

        @Parcelize
        data object ChangeAccountProvider : NavTarget

        @Parcelize
        data object SearchAccountProvider : NavTarget

        @Parcelize
        data object LoginPassword : NavTarget

        @Parcelize
        data class WaitList(val loginFormState: LoginFormState) : NavTarget

        @Parcelize
        data class OidcView(val oidcDetails: OidcDetails) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                if (plugins<Inputs>().first().isQrCode) {
                    resolve(NavTarget.QrCode, buildContext)
                } else {
                    resolve(NavTarget.ConfirmAccountProvider, buildContext)
                }
            }
            NavTarget.ConfirmAccountProvider -> {
                val inputs = ConfirmAccountProviderNode.Inputs(
                    isAccountCreation = inputs.isAccountCreation
                )
                val callback = object : ConfirmAccountProviderNode.Callback {
                    override fun onOidcDetails(oidcDetails: OidcDetails) {
                        if (customTabAvailabilityChecker.supportCustomTab()) {
                            // In this case open a Chrome Custom tab
                            activity?.let {
                                customChromeTabStarted = true
                                customTabHandler.open(it, darkTheme, oidcDetails.url)
                            }
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
                val callback = object : LoginPasswordNode.Callback {
                    override fun onWaitListError(loginFormState: LoginFormState) {
                        backstack.newRoot(NavTarget.WaitList(loginFormState))
                    }
                }
                createNode<LoginPasswordNode>(buildContext, plugins = listOf(callback))
            }
            is NavTarget.OidcView -> {
                val input = OidcNode.Inputs(navTarget.oidcDetails)
                createNode<OidcNode>(buildContext, plugins = listOf(input))
            }
            is NavTarget.WaitList -> {
                val inputs = WaitListNode.Inputs(
                    loginFormState = navTarget.loginFormState,
                )
                val callback = object : WaitListNode.Callback {
                    override fun onCancelClicked() {
                        navigateUp()
                    }
                }
                createNode<WaitListNode>(buildContext, plugins = listOf(callback, inputs))
            }
            NavTarget.QrCode -> TODO()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        activity = LocalContext.current as? Activity
        darkTheme = !ElementTheme.isLightTheme
        DisposableEffect(Unit) {
            onDispose {
                activity = null
                accountProviderDataSource.reset()
            }
        }
        BackstackView()
    }
}
