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
import io.element.android.features.onboarding.api.OnBoardingEntryPoint
import io.element.android.features.preferences.api.ConfigureTracingEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
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
        data class LoginFlow(
            val isAccountCreation: Boolean,
            val isQrCode: Boolean,
        ) : NavTarget

        @Parcelize
        data object ConfigureTracing : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.OnBoarding -> {
                val callback = object : OnBoardingEntryPoint.Callback {
                    override fun onSignUp() {
                        backstack.push(NavTarget.LoginFlow(isAccountCreation = true, isQrCode = false))
                    }

                    override fun onSignIn() {
                        backstack.push(NavTarget.LoginFlow(isAccountCreation = false, isQrCode = false))
                    }

                    override fun onSignInWithQrCode() {
                        backstack.push(NavTarget.LoginFlow(isAccountCreation = false, isQrCode = true))
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
                    .params(LoginEntryPoint.Params(isAccountCreation = navTarget.isAccountCreation, isQrCode = navTarget.isQrCode))
                    .build()
            }
            NavTarget.ConfigureTracing -> {
                configureTracingEntryPoint.createNode(this, buildContext)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
