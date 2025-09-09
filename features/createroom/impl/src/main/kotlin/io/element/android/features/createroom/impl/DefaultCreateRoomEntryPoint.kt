/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
@Inject class DefaultCreateRoomEntryPoint : CreateRoomEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): CreateRoomEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : CreateRoomEntryPoint.NodeBuilder {
            override fun callback(callback: CreateRoomEntryPoint.Callback): CreateRoomEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<CreateRoomFlowNode>(buildContext, plugins)
            }
        }
    }
}
