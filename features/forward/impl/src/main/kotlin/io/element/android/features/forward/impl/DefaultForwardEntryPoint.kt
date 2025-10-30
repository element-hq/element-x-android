/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.forward.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.forward.api.ForwardEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultForwardEntryPoint : ForwardEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): ForwardEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : ForwardEntryPoint.NodeBuilder {
            override fun params(params: ForwardEntryPoint.Params): ForwardEntryPoint.NodeBuilder {
                plugins += ForwardMessagesNode.Inputs(
                    eventId = params.eventId,
                    timelineProvider = params.timelineProvider,
                )
                return this
            }

            override fun callback(callback: ForwardEntryPoint.Callback): ForwardEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<ForwardMessagesNode>(buildContext, plugins)
            }
        }
    }
}
