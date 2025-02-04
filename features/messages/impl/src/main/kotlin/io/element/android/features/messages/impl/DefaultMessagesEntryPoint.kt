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
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultMessagesEntryPoint @Inject constructor() : MessagesEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): MessagesEntryPoint.NodeBuilder {
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
                return parentNode.createNode<MessagesFlowNode>(buildContext, plugins)
            }
        }
    }
}

internal fun MessagesEntryPoint.InitialTarget.toNavTarget() = when (this) {
    is MessagesEntryPoint.InitialTarget.Messages -> MessagesFlowNode.NavTarget.Messages(focusedEventId)
    MessagesEntryPoint.InitialTarget.PinnedMessages -> MessagesFlowNode.NavTarget.PinnedMessagesList
}
