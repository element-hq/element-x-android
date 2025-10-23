/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultMessagesEntryPoint : MessagesEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): MessagesEntryPoint.NodeBuilder {
        val nodeFactories = parentNode.bindings<NodeFactoriesBindings>().nodeFactories()
        val plugins = ArrayList<Plugin>()

        return object : MessagesEntryPoint.NodeBuilder {
            override fun params(params: MessagesEntryPoint.Params): MessagesEntryPoint.NodeBuilder {
                plugins += MessagesEntryPoint.Params(params.initialTarget)
                return this
            }

            override fun callback(callback: MessagesEntryPoint.Callback): MessagesEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return nodeFactories[MessagesFlowNode::class]!!.create(buildContext, plugins)
            }
        }
    }
}

internal fun MessagesEntryPoint.InitialTarget.toNavTarget() = when (this) {
    is MessagesEntryPoint.InitialTarget.Messages -> MessagesFlowNode.NavTarget.Messages(focusedEventId, inThreadId)
    MessagesEntryPoint.InitialTarget.PinnedMessages -> MessagesFlowNode.NavTarget.PinnedMessagesList
}
