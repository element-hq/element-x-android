/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.messagesearch.api.MessageSearchEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesBinding(AppScope::class)
class DefaultMessageSearchEntryPoint : MessageSearchEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        roomId: RoomId?,
        callback: MessageSearchEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<MessageSearchNode>(
            buildContext,
            plugins = listOf(MessageSearchNode.Inputs(roomId), callback),
        )
    }
}
