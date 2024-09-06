/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.Coil
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.features.login.api.LoginFlowType
import io.element.android.features.onboarding.api.OnBoardingEntryPoint
import io.element.android.features.preferences.api.ConfigureTracingEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.designsystem.utils.ForceOrientationInMobileDevices
import io.element.android.libraries.designsystem.utils.ScreenOrientation
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.ui.media.NotLoggedInImageLoaderFactory
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class NotLoggedInFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val onBoardingEntryPoint: OnBoardingEntryPoint,
    private val configureTracingEntryPoint: ConfigureTracingEntryPoint,
    private val loginEntryPoint: LoginEntryPoint,
    private val notLoggedInImageLoaderFactory: NotLoggedInImageLoaderFactory,
) : BaseFlowNode<NotLoggedInFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.OnBoarding,
        savedStateMap = buildContext.savedStateMap
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun onOpenBugReport()
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                Coil.setImageLoader(notLoggedInImageLoaderFactory)
            },
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object OnBoarding : NavTarget

        @Parcelize
        data class LoginFlow(val type: LoginFlowType) : NavTarget

        @Parcelize
        data object ConfigureTracing : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.OnBoarding -> {
                val callback = object : OnBoardingEntryPoint.Callback {
                    override fun onSignUp() {
                        backstack.push(NavTarget.LoginFlow(type = LoginFlowType.SIGN_UP))
                    }

                    override fun onSignIn() {
                        backstack.push(NavTarget.LoginFlow(type = LoginFlowType.SIGN_IN_MANUAL))
                    }

                    override fun onSignInWithQrCode() {
                        backstack.push(NavTarget.LoginFlow(type = LoginFlowType.SIGN_IN_QR_CODE))
                    }

                    override fun onOpenDeveloperSettings() {
                        backstack.push(NavTarget.ConfigureTracing)
                    }

                    override fun onReportProblem() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }
                }
                onBoardingEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            is NavTarget.LoginFlow -> {
                loginEntryPoint.nodeBuilder(this, buildContext)
                    .params(LoginEntryPoint.Params(flowType = navTarget.type))
                    .build()
            }
            NavTarget.ConfigureTracing -> {
                configureTracingEntryPoint.createNode(this, buildContext)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        // The login flow doesn't support landscape mode on mobile devices yet
        ForceOrientationInMobileDevices(orientation = ScreenOrientation.PORTRAIT)

        BackstackView()
    }
}
