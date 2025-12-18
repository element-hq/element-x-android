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
    data class SendError(val transactionId: TransactionId) : SendQueueUpdate
    data class RetrySendingEvent(val transactionId: TransactionId) : SendQueueUpdate
    data class SentEvent(val transactionId: TransactionId, val eventId: EventId) : SendQueueUpdate
    data class MediaUpload(val relatedTo: EventId, val file: MediaSource?, val index: Long, val progress: Float) : SendQueueUpdate
}
