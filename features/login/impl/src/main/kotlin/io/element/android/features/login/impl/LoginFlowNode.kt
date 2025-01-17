/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.api.LoginFlowType
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.qrcode.QrCodeLoginFlowNode
import io.element.android.features.login.impl.screens.changeaccountprovider.ChangeAccountProviderNode
import io.element.android.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderNode
import io.element.android.features.login.impl.screens.createaccount.CreateAccountNode
import io.element.android.features.login.impl.screens.loginpassword.LoginPasswordNode
import io.element.android.features.login.impl.screens.searchaccountprovider.SearchAccountProviderNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import io.element.android.libraries.oidc.api.OidcEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class LoginFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val defaultLoginUserStory: DefaultLoginUserStory,
    private val oidcActionFlow: OidcActionFlow,
    private val oidcEntryPoint: OidcEntryPoint,
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
        val flowType: LoginFlowType,
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
        data object ConfirmAccountProvider : NavTarget

        @Parcelize
        data object ChangeAccountProvider : NavTarget

        @Parcelize
        data object SearchAccountProvider : NavTarget

        @Parcelize
        data object LoginPassword : NavTarget

        @Parcelize
        data class CreateAccount(val url: String) : NavTarget

        @Parcelize
        data class OidcView(val oidcDetails: OidcDetails) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                if (inputs.flowType == LoginFlowType.SIGN_IN_QR_CODE) {
                    createNode<QrCodeLoginFlowNode>(buildContext)
                } else {
                    resolve(NavTarget.ConfirmAccountProvider, buildContext)
                }
            }
            NavTarget.ConfirmAccountProvider -> {
                val inputs = ConfirmAccountProviderNode.Inputs(
                    isAccountCreation = inputs.flowType == LoginFlowType.SIGN_UP,
                )
                val callback = object : ConfirmAccountProviderNode.Callback {
                    override fun onOidcDetails(oidcDetails: OidcDetails) {
                        if (oidcEntryPoint.canUseCustomTab()) {
                            // In this case open a Chrome Custom tab
                            activity?.let {
                                customChromeTabStarted = true
                                oidcEntryPoint.openUrlInCustomTab(it, darkTheme, oidcDetails.url)
                            }
                        } else {
                            // Fallback to WebView mode
                            backstack.push(NavTarget.OidcView(oidcDetails))
                        }
                    }

                    override fun onCreateAccountContinue(url: String) {
                        backstack.push(NavTarget.CreateAccount(url))
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

                    override fun onOtherClick() {
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
                createNode<LoginPasswordNode>(buildContext)
            }
            is NavTarget.OidcView -> {
                oidcEntryPoint.createFallbackWebViewNode(this, buildContext, navTarget.oidcDetails.url)
            }
            is NavTarget.CreateAccount -> {
                val inputs = CreateAccountNode.Inputs(
                    url = navTarget.url,
                )
                createNode<CreateAccountNode>(buildContext, listOf(inputs))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        activity = LocalActivity.current
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
