/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId

sealed interface MessagesEvents {
    data class HandleAction(val action: TimelineItemAction, val event: TimelineItem.Event) : MessagesEvents
    data class ToggleReaction(val emoji: String, val eventOrTransactionId: EventOrTransactionId) : MessagesEvents
    data class InviteDialogDismissed(val action: InviteDialogAction) : MessagesEvents
    data object Dismiss : MessagesEvents
}

enum class InviteDialogAction {
    Cancel,
    Invite,
}
