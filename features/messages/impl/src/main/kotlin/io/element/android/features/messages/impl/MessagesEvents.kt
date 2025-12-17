/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface MessagesEvents {
    data class HandleAction(val action: TimelineItemAction, val event: TimelineItem.Event) : MessagesEvents
    data class ToggleReaction(val emoji: String, val eventOrTransactionId: EventOrTransactionId) : MessagesEvents
    data class InviteDialogDismissed(val action: InviteDialogAction) : MessagesEvents
    data class OnUserClicked(val user: MatrixUser) : MessagesEvents
    data object Dismiss : MessagesEvents
    data object MarkAsFullyReadAndExit : MessagesEvents
}

enum class InviteDialogAction {
    Cancel,
    Invite,
}
