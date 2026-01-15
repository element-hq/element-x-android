/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.EventThreadInfo
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import kotlinx.collections.immutable.ImmutableList

data class EventTimelineItem(
    val eventId: EventId?,
    val transactionId: TransactionId?,
    val isEditable: Boolean,
    val canBeRepliedTo: Boolean,
    val isOwn: Boolean,
    val isRemote: Boolean,
    val localSendState: LocalEventSendState?,
    val reactions: ImmutableList<EventReaction>,
    val receipts: ImmutableList<Receipt>,
    val sender: UserId,
    val senderProfile: ProfileDetails,
    val timestamp: Long,
    val content: EventContent,
    val origin: TimelineItemEventOrigin?,
    val timelineItemDebugInfoProvider: TimelineItemDebugInfoProvider,
    val messageShieldProvider: MessageShieldProvider,
    val sendHandleProvider: SendHandleProvider,
    /**
     * If the keys to this message were forwarded by another user via history sharing (MSC4268), the ID of that user.
     * If this is set, then [messageShieldProvider] will also return [MessageShield.AuthenticityNotGuaranteed].
     */
    val forwarder: UserId?,
    /** If [forwarder] is set, the profile of the forwarding user, if it was cached at the time this `EventTimelineItem` was created. */
    val forwarderProfile: ProfileDetails?,
) {
    fun inReplyTo(): InReplyTo? {
        return (content as? MessageContent)?.inReplyTo
    }

    fun threadInfo(): EventThreadInfo? = when (content) {
        is MessageContent -> content.threadInfo
        is PollContent -> content.threadInfo
        is StickerContent -> content.threadInfo
        is UnableToDecryptContent -> content.threadInfo
        else -> null
    }

    fun hasNotLoadedInReplyTo(): Boolean {
        val details = inReplyTo()
        return details is InReplyTo.NotLoaded
    }
}

fun interface TimelineItemDebugInfoProvider {
    operator fun invoke(): TimelineItemDebugInfo
}

fun interface MessageShieldProvider {
    operator fun invoke(strict: Boolean): MessageShield?
}

fun interface SendHandleProvider {
    operator fun invoke(): SendHandle?
}
