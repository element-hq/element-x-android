/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.home.api.HomeEntryPoint
import io.element.android.libraries.architecture.createNode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultHomeEntryPoint() : HomeEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): HomeEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : HomeEntryPoint.NodeBuilder {
            override fun callback(callback: HomeEntryPoint.Callback): HomeEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<HomeFlowNode>(buildContext, plugins)
            }
        }
    }
}
