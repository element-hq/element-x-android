/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.roomdirectory.api.RoomDirectoryEntryPoint
import io.element.android.features.roomdirectory.impl.root.RoomDirectoryNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRoomDirectoryEntryPoint @Inject constructor() : RoomDirectoryEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): RoomDirectoryEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : RoomDirectoryEntryPoint.NodeBuilder {
            override fun callback(callback: RoomDirectoryEntryPoint.Callback): RoomDirectoryEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<RoomDirectoryNode>(buildContext, plugins)
            }
        }
    }
}
