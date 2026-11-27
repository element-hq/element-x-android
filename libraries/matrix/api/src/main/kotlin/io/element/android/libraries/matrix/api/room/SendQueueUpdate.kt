/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.MediaSource

sealed interface SendQueueUpdate {
    data class NewLocalEvent(val transactionId: TransactionId) : SendQueueUpdate
    data class CancelledLocalEvent(val transactionId: TransactionId) : SendQueueUpdate
    data class ReplacedLocalEvent(val transactionId: TransactionId) : SendQueueUpdate

    /**
     * @param transactionId the transaction id of the local event which failed to be sent.
     * @param isRecoverable whether the error is considered recoverable or not.
     * A recoverable error disables the room's send queue, while an unrecoverable error
     * is parked until the user decides to cancel sending it.
     */
    data class SendError(val transactionId: TransactionId, val isRecoverable: Boolean) : SendQueueUpdate
    data class RetrySendingEvent(val transactionId: TransactionId) : SendQueueUpdate
    data class SentEvent(val transactionId: TransactionId, val eventId: EventId) : SendQueueUpdate
    data class MediaUpload(val relatedTo: TransactionId, val file: MediaSource?, val index: Long, val progress: Float) : SendQueueUpdate
}
