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
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.qrcode.QrCodeLoginFlowNode
import io.element.android.features.login.impl.screens.changeaccountprovider.ChangeAccountProviderNode
import io.element.android.features.login.impl.screens.chooseaccountprovider.ChooseAccountProviderNode
import io.element.android.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderNode
import io.element.android.features.login.impl.screens.createaccount.CreateAccountNode
import io.element.android.features.login.impl.screens.loginpassword.LoginPasswordNode
import io.element.android.features.login.impl.screens.onboarding.OnBoardingNode
import io.element.android.features.login.impl.screens.searchaccountprovider.SearchAccountProviderNode
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
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
) : BaseFlowNode<LoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.OnBoarding,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    data class Params(
        val accountProvider: String?,
        val loginHint: String?,
    ) : NodeInputs

    private var activity: Activity? = null
    private var darkTheme: Boolean = false

    private var externalAppStarted = false

    override fun onBuilt() {
        super.onBuilt()
        defaultLoginUserStory.setLoginFlowIsDone(false)
        lifecycle.subscribe(
            onResume = {
                if (externalAppStarted) {
                    externalAppStarted = false
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
        data object OnBoarding : NavTarget

        @Parcelize
        data object QrCode : NavTarget

        @Parcelize
        data class ConfirmAccountProvider(
            val isAccountCreation: Boolean,
        ) : NavTarget

        @Parcelize
        data object ChooseAccountProvider : NavTarget

        @Parcelize
        data object ChangeAccountProvider : NavTarget

        @Parcelize
        data object SearchAccountProvider : NavTarget

        @Parcelize
        data object LoginPassword : NavTarget

        @Parcelize
        data class CreateAccount(val url: String) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.OnBoarding -> {
                val callback = object : OnBoardingNode.Callback {
                    override fun onSignUp() {
                        backstack.push(
                            NavTarget.ConfirmAccountProvider(isAccountCreation = true)
                        )
                    }

                    override fun onSignIn(mustChooseAccountProvider: Boolean) {
                        backstack.push(
                            if (mustChooseAccountProvider) {
                                NavTarget.ChooseAccountProvider
                            } else {
                                NavTarget.ConfirmAccountProvider(isAccountCreation = false)
                            }
                        )
                    }

                    override fun onSignInWithQrCode() {
                        backstack.push(NavTarget.QrCode)
                    }

                    override fun onReportProblem() {
                        plugins<LoginEntryPoint.Callback>().forEach { it.onReportProblem() }
                    }

                    override fun onOidcDetails(oidcDetails: OidcDetails) {
                        navigateToMas(oidcDetails)
                    }

                    override fun onCreateAccountContinue(url: String) {
                        backstack.push(NavTarget.CreateAccount(url))
                    }

                    override fun onLoginPasswordNeeded() {
                        backstack.push(NavTarget.LoginPassword)
                    }
                }
                val params = inputs<Params>()
                val inputs = OnBoardingNode.Params(
                    accountProvider = params.accountProvider,
                    loginHint = params.loginHint,
                )
                createNode<OnBoardingNode>(buildContext, listOf(callback, inputs))
            }
            NavTarget.ChooseAccountProvider -> {
                val callback = object : ChooseAccountProviderNode.Callback {
                    override fun onOidcDetails(oidcDetails: OidcDetails) {
                        navigateToMas(oidcDetails)
                    }

                    override fun onCreateAccountContinue(url: String) {
                        backstack.push(NavTarget.CreateAccount(url))
                    }

                    override fun onLoginPasswordNeeded() {
                        backstack.push(NavTarget.LoginPassword)
                    }
                }
                createNode<ChooseAccountProviderNode>(buildContext, listOf(callback))
            }
            NavTarget.QrCode -> {
                createNode<QrCodeLoginFlowNode>(buildContext)
            }
            is NavTarget.ConfirmAccountProvider -> {
                val inputs = ConfirmAccountProviderNode.Inputs(
                    isAccountCreation = navTarget.isAccountCreation,
                )
                val callback = object : ConfirmAccountProviderNode.Callback {
                    override fun onOidcDetails(oidcDetails: OidcDetails) {
                        navigateToMas(oidcDetails)
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
                        val confirmAccountProvider = backstack.elements.value.firstOrNull {
                            it.key.navTarget is NavTarget.ConfirmAccountProvider
                        }?.key?.navTarget ?: NavTarget.ConfirmAccountProvider(isAccountCreation = false)
                        backstack.singleTop(confirmAccountProvider)
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
                        val confirmAccountProvider = backstack.elements.value.firstOrNull {
                            it.key.navTarget is NavTarget.ConfirmAccountProvider
                        }?.key?.navTarget ?: NavTarget.ConfirmAccountProvider(isAccountCreation = false)
                        backstack.singleTop(confirmAccountProvider)
                    }
                }

                createNode<SearchAccountProviderNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.LoginPassword -> {
                createNode<LoginPasswordNode>(buildContext)
            }
            is NavTarget.CreateAccount -> {
                val inputs = CreateAccountNode.Inputs(
                    url = navTarget.url,
                )
                createNode<CreateAccountNode>(buildContext, listOf(inputs))
            }
        }
    }

    private fun navigateToMas(oidcDetails: OidcDetails) {
        activity?.let {
            externalAppStarted = true
            it.openUrlInChromeCustomTab(null, darkTheme, oidcDetails.url)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        activity = requireNotNull(LocalActivity.current)
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
