/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import kotlinx.parcelize.Parcelize

interface MessagesEntryPoint : FeatureEntryPoint {
    sealed interface InitialTarget : Parcelable {
        @Parcelize
        data class Messages(
            val focusedEventId: EventId?,
        ) : InitialTarget

        @Parcelize
        data object PinnedMessages : InitialTarget
    }

    interface Callback : Plugin {
        fun navigateToRoomDetails()
        fun navigateToRoomMemberDetails(userId: UserId)
        fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean)
        fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean)
        fun navigateToRoom(roomId: RoomId)
    }

    data class Params(val initialTarget: InitialTarget) : NodeInputs

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    interface NodeProxy {
        suspend fun attachThread(threadId: ThreadId, focusedEventId: EventId?)
    }
}
