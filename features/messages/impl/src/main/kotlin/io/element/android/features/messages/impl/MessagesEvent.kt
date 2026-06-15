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

sealed interface MessagesEvent {
    data class HandleAction(val action: TimelineItemAction, val event: TimelineItem.Event) : MessagesEvent
    data class ToggleReaction(val emoji: String, val eventOrTransactionId: EventOrTransactionId) : MessagesEvent
    data class InviteDialogDismissed(val action: InviteDialogAction) : MessagesEvent
    data class OnUserClicked(val user: MatrixUser) : MessagesEvent
    data object StopLiveLocationShare : MessagesEvent
    data object ShowLiveLocationShare : MessagesEvent
    data object MarkAsFullyReadAndExit : MessagesEvent

    /** Enter selection mode anchored on a single event (the user long-pressed or hit "Select"). */
    data class EnterSelection(val anchor: TimelineItem.Event) : MessagesEvent

    /** Add or remove an event from the active selection. No-op outside selection mode. */
    data class ToggleSelection(val event: TimelineItem.Event) : MessagesEvent

    /** Exit selection mode and clear the set. */
    data object ClearSelection : MessagesEvent

    /** Redact every event in the selection sequentially. */
    data object BulkRedactSelected : MessagesEvent

    /** Join selected message bodies and write to clipboard. */
    data object BulkCopySelected : MessagesEvent

    /** Open the forward picker pre-loaded with every selected event. */
    data object BulkForwardSelected : MessagesEvent
}

enum class InviteDialogAction {
    Cancel,
    Invite,
}
