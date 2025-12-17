/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(DelicateCoilApi::class)

package io.element.android.appnav

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.di.SessionGraphFactory
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import kotlinx.parcelize.Parcelize

/**
 * `LoggedInAppScopeFlowNode` is a Node responsible to set up the Session graph.
 * [io.element.android.libraries.di.SessionScope]. It has only one child: [LoggedInFlowNode].
 * This allow to inject objects with SessionScope in the constructor of [LoggedInFlowNode].
 */
@ContributesNode(AppScope::class)
@AssistedInject
class LoggedInAppScopeFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    sessionGraphFactory: SessionGraphFactory,
    private val imageLoaderHolder: ImageLoaderHolder,
) : ParentNode<LoggedInAppScopeFlowNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
), DependencyInjectionGraphOwner {
    interface Callback : Plugin {
        fun navigateToBugReport()
        fun navigateToAddAccount()
    }

    private val callback: Callback = callback()

    @Parcelize
    object NavTarget : Parcelable

    data class Inputs(
        val matrixClient: MatrixClient
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    override val graph = sessionGraphFactory.create(inputs.matrixClient)

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onResume = {
                SingletonImageLoader.setUnsafe(imageLoaderHolder.get(inputs.matrixClient))
            },
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        val callback = object : LoggedInFlowNode.Callback {
            override fun navigateToBugReport() {
                callback.navigateToBugReport()
            }

            override fun navigateToAddAccount() {
                callback.navigateToAddAccount()
            }
        }
        return createNode<LoggedInFlowNode>(buildContext, listOf(callback))
    }

    suspend fun attachSession(): LoggedInFlowNode = waitForChildAttached()

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = navModel,
            modifier = modifier,
        )
    }
}
