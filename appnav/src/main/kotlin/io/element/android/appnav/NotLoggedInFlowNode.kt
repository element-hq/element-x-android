/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(DelicateCoilApi::class)

package io.element.android.appnav

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.features.login.api.LoginParams
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.utils.ForceOrientationInMobileDevices
import io.element.android.libraries.designsystem.utils.ScreenOrientation
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.ui.media.NotLoggedInImageLoaderFactory
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class NotLoggedInFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val loginEntryPoint: LoginEntryPoint,
    private val notLoggedInImageLoaderFactory: NotLoggedInImageLoaderFactory,
) : BaseFlowNode<NotLoggedInFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    data class Params(
        val loginParams: LoginParams?,
    ) : NodeInputs

    interface Callback : Plugin {
        fun onOpenBugReport()
    }

    private val inputs = inputs<Params>()

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                SingletonImageLoader.setUnsafe(notLoggedInImageLoaderFactory.newImageLoader())
            },
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : LoginEntryPoint.Callback {
                    override fun onReportProblem() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }
                }
                loginEntryPoint
                    .nodeBuilder(this, buildContext)
                    .params(
                        LoginEntryPoint.Params(
                            accountProvider = inputs.loginParams?.accountProvider,
                            loginHint = inputs.loginParams?.loginHint,
                        )
                    )
                    .callback(callback)
                    .build()
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
