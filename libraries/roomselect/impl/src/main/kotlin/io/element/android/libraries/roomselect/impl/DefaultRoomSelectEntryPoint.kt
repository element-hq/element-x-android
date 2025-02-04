/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultRoomSelectEntryPoint @Inject constructor() : RoomSelectEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): RoomSelectEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : RoomSelectEntryPoint.NodeBuilder {
            override fun params(params: RoomSelectEntryPoint.Params): RoomSelectEntryPoint.NodeBuilder {
                plugins += RoomSelectNode.Inputs(mode = params.mode)
                return this
            }

            override fun callback(callback: RoomSelectEntryPoint.Callback): RoomSelectEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<RoomSelectNode>(buildContext, plugins)
            }
        }
    }
}
