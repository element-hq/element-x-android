/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId

interface MessageSearchEntryPoint : FeatureEntryPoint {
    /**
     * Build the message search node.
     *
     * @param parentNode the node the search node is attached to.
     * @param buildContext the Appyx build context for the new node.
     * @param roomId when non-null the search is scoped to that room only; null searches every room.
     * @param callback invoked when the user picks a result and the host should navigate to it.
     */
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        roomId: RoomId?,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        fun navigateToEvent(roomId: RoomId, eventId: EventId)
    }
}
