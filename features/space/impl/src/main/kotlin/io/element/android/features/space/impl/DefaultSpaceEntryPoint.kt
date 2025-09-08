/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultSpaceEntryPoint @Inject constructor() : SpaceEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): SpaceEntryPoint.NodeBuilder {
        val plugins = mutableSetOf<Plugin>()
        return object : SpaceEntryPoint.NodeBuilder {
            override fun params(params: SpaceEntryPoint.Params): SpaceEntryPoint.NodeBuilder {
                plugins.add(params)
                return this
            }

            override fun callback(callback: SpaceEntryPoint.Callback): SpaceEntryPoint.NodeBuilder {
                plugins.add(callback)
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<SpaceNode>(buildContext, plugins = plugins.toList())
            }
        }
    }
}
