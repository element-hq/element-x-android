/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint

@ContributesBinding(AppScope::class)
@Inject
class DefaultPushHistoryEntryPoint : PushHistoryEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): PushHistoryEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : PushHistoryEntryPoint.NodeBuilder {
            override fun callback(callback: PushHistoryEntryPoint.Callback): PushHistoryEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<PushHistoryNode>(buildContext, plugins)
            }
        }
    }
}
