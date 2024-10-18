/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId

@Immutable
sealed interface EventOrTransactionId {
    @JvmInline
    value class Event(val id: EventId) : EventOrTransactionId

    @JvmInline
    value class Transaction(val id: TransactionId) : EventOrTransactionId

    val eventId: EventId?
        get() = (this as? Event)?.id

    companion object {
        fun from(eventId: EventId?, transactionId: TransactionId?): EventOrTransactionId {
            return when {
                eventId != null -> Event(eventId)
                transactionId != null -> Transaction(transactionId)
                else -> throw IllegalArgumentException("EventId and TransactionId are both null")
            }
        }
    }
}

fun EventId.toEventOrTransactionId() = EventOrTransactionId.Event(this)
fun TransactionId.toEventOrTransactionId() = EventOrTransactionId.Transaction(this)
