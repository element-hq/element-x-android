/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.SendTarget
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import timber.log.Timber

/**
 * A reaction that failed to send with an unrecoverable error blocks the room's send queue: none of the
 * events queued after it are sent until it is retried or aborted. There is no UI to retry or remove a
 * pending reaction, so abort it to unblock the sending of the next events.
 */
class WedgedReactionsAborter(
    private val abortSend: suspend (EventOrTransactionId, SendTarget) -> Result<Boolean>,
) {
    private val abortedReactions = mutableSetOf<Pair<EventOrTransactionId, String>>()

    suspend fun onEachMatrixTimelineItem(timelineItems: List<MatrixTimelineItem>) {
        timelineItems
            .filterIsInstance<MatrixTimelineItem.Event>()
            .flatMap { item ->
                val eventOrTransactionId = item.event.eventId?.toEventOrTransactionId()
                    ?: item.event.transactionId?.toEventOrTransactionId()
                    ?: return@flatMap emptyList()
                item.event.reactions
                    .filter { reaction -> reaction.senders.any { it.isSendWedged } }
                    .map { reaction -> eventOrTransactionId to reaction.key }
            }
            .filter { it !in abortedReactions }
            .forEach { reaction ->
                val (eventOrTransactionId, key) = reaction
                Timber.w("Aborting reaction to $eventOrTransactionId which failed to send and blocks the send queue")
                abortedReactions.add(reaction)
                abortSend(eventOrTransactionId, SendTarget.Reaction(key)).onFailure {
                    Timber.e(it, "Failed to abort reaction to $eventOrTransactionId")
                    // Try again on the next update
                    abortedReactions.remove(reaction)
                }
            }
    }
}
