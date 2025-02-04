/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.roomaliasesolver.api.RoomAliasResolverEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRoomAliasResolverEntryPoint @Inject constructor() : RoomAliasResolverEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): RoomAliasResolverEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : RoomAliasResolverEntryPoint.NodeBuilder {
            override fun callback(callback: RoomAliasResolverEntryPoint.Callback): RoomAliasResolverEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun params(params: RoomAliasResolverEntryPoint.Params): RoomAliasResolverEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<RoomAliasResolverNode>(buildContext, plugins)
            }
        }
    }
}
