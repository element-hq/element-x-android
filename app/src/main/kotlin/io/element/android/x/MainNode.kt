/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.x

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.appnav.RootFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.DaggerComponentOwner
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class MainNode(
    buildContext: BuildContext,
    plugins: List<Plugin>,
    @ApplicationContext context: Context,
) : ParentNode<MainNode.RootNavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(RootNavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
),
    DaggerComponentOwner {
    override val daggerComponent = (context as DaggerComponentOwner).daggerComponent

    override fun resolve(navTarget: RootNavTarget, buildContext: BuildContext): Node {
        return createNode<RootFlowNode>(buildContext = buildContext)
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = navModel)
    }

    fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            waitForChildAttached<RootFlowNode>().handleIntent(intent)
        }
    }

    @Parcelize
    object RootNavTarget : Parcelable
}
