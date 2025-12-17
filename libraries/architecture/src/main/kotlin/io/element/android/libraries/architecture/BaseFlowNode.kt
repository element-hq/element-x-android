/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.children.ChildEntry
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.combined.plus
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.navigation.transition.TransitionHandler
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import io.element.android.libraries.architecture.overlay.Overlay

/**
 * This class is a [ParentNode] that contains a [BackStack] and an [Overlay]. It is used to represent a flow in the app.
 * Should be used instead of [ParentNode] in flow nodes.
 */
@Stable
abstract class BaseFlowNode<NavTarget : Any>(
    val backstack: BackStack<NavTarget>,
    buildContext: BuildContext,
    plugins: List<Plugin>,
    val overlay: Overlay<NavTarget> = Overlay(null),
    val permanentNavModel: PermanentNavModel<NavTarget> = PermanentNavModel(emptySet(), null),
    childKeepMode: ChildEntry.KeepMode = ChildEntry.KeepMode.KEEP,
) : ParentNode<NavTarget>(
    navModel = overlay + backstack + permanentNavModel,
    buildContext = buildContext,
    plugins = plugins,
    childKeepMode = childKeepMode,
) {
    override fun onBuilt() {
        super.onBuilt()
        lifecycle.logLifecycle(this::class.java.simpleName)
        whenChildAttached<Node> { _, child ->
            // BackstackNode will be logged by their parent.
            if (child !is BaseFlowNode<*>) {
                child.lifecycle.logLifecycle(child::class.java.simpleName)
            }
        }
    }
}

@Composable
inline fun <reified NavTarget : Any> BaseFlowNode<NavTarget>.BackstackView(
    modifier: Modifier = Modifier,
    transitionHandler: TransitionHandler<NavTarget, BackStack.State> = rememberBackstackSlider(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
    ),
) {
    Children(
        modifier = modifier,
        navModel = backstack,
        transitionHandler = transitionHandler,
    )
}

@Composable
inline fun <reified NavTarget : Any> BaseFlowNode<NavTarget>.OverlayView(
    modifier: Modifier = Modifier,
    transitionHandler: TransitionHandler<NavTarget, BackStack.State> = rememberBackstackFader(),
) {
    Children(
        modifier = modifier,
        navModel = overlay,
        transitionHandler = transitionHandler,
    )
}

@Composable
inline fun <reified NavTarget : Any> BaseFlowNode<NavTarget>.BackstackWithOverlayBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier) {
        BackstackView()
        OverlayView()
        content()
    }
}
