/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.login.impl.screens.classic.loginwithclassic.LoginWithClassicNode
import io.element.android.features.login.impl.screens.classic.missingkeybackup.MissingKeyBackupNode
import io.element.android.features.login.impl.screens.classic.root.RootNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.appyx.rememberFaderOrSliderTransitionHandler
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
@AssistedInject
class ClassicFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val classicFlowNodeHelper: ClassicFlowNodeHelper,
) : BaseFlowNode<ClassicFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun navigateToOnBoarding(allowBackNavigation: Boolean)
        fun navigateToLoginPassword()
        fun navigateToOAuth(oAuthDetails: OAuthDetails)
        fun navigateToCreateAccount(url: String)
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class LoginWithClassic(
            val userId: UserId,
        ) : NavTarget

        @Parcelize
        data object MissingKeyBackup : NavTarget
    }

    private val callback: Callback = callback()

    override fun onBuilt() {
        super.onBuilt()
        observeElementClassicConnection()
        lifecycle.subscribe(
            onResume = {
                classicFlowNodeHelper.onResume()
            },
        )
    }

    private fun observeElementClassicConnection() {
        classicFlowNodeHelper.navigationEventFlow().onEach { navigationEvent ->
            when (navigationEvent) {
                is NavigationEvent.Idle -> Unit
                is NavigationEvent.NavigateToOnBoarding -> callback.navigateToOnBoarding(allowBackNavigation = false)
                is NavigationEvent.NavigateToLoginWithClassic -> backstack.newRoot(NavTarget.LoginWithClassic(navigationEvent.userId))
            }
        }.launchIn(lifecycleScope)
    }

    override fun resolve(
        navTarget: NavTarget,
        buildContext: BuildContext,
    ): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                createNode<RootNode>(buildContext)
            }
            is NavTarget.LoginWithClassic -> {
                val callback = object : LoginWithClassicNode.Callback {
                    override fun navigateToOtherOptions() {
                        callback.navigateToOnBoarding(allowBackNavigation = true)
                    }

                    override fun navigateToLoginPassword() {
                        callback.navigateToLoginPassword()
                    }

                    override fun navigateToOAuth(oAuthDetails: OAuthDetails) {
                        callback.navigateToOAuth(oAuthDetails)
                    }

                    override fun navigateToCreateAccount(url: String) {
                        callback.navigateToCreateAccount(url)
                    }

                    override fun navigateToMissingKeyBackup() {
                        backstack.push(NavTarget.MissingKeyBackup)
                    }
                }
                val inputs = LoginWithClassicNode.Inputs(
                    userId = navTarget.userId,
                )
                createNode<LoginWithClassicNode>(buildContext, plugins = listOf(inputs, callback))
            }
            NavTarget.MissingKeyBackup -> {
                val callback = object : MissingKeyBackupNode.Callback {
                    override fun navigateBack() {
                        backstack.pop()
                    }
                }
                createNode<MissingKeyBackupNode>(buildContext, listOf(callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(
            modifier = modifier,
            transitionHandler = rememberFaderOrSliderTransitionHandler(),
        )
    }
}
