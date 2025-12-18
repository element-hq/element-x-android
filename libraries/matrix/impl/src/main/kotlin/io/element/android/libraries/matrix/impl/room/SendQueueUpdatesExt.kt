/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.room.SendQueueUpdate
import io.element.android.libraries.matrix.impl.media.map
import org.matrix.rustcomponents.sdk.RoomSendQueueUpdate

fun RoomSendQueueUpdate.map(): SendQueueUpdate = when (this) {
    is RoomSendQueueUpdate.NewLocalEvent -> SendQueueUpdate.NewLocalEvent(TransactionId(transactionId))
    is RoomSendQueueUpdate.CancelledLocalEvent -> SendQueueUpdate.CancelledLocalEvent(TransactionId(transactionId))
    is RoomSendQueueUpdate.MediaUpload -> SendQueueUpdate.MediaUpload(
        relatedTo = EventId(relatedTo),
        file = file?.map(),
        index = index.toLong(),
        progress = progress.current.toFloat() / progress.total.toFloat(),
    )
    is RoomSendQueueUpdate.ReplacedLocalEvent -> SendQueueUpdate.ReplacedLocalEvent(TransactionId(transactionId))
    is RoomSendQueueUpdate.RetryEvent -> SendQueueUpdate.RetrySendingEvent(TransactionId(transactionId))
    is RoomSendQueueUpdate.SendError -> SendQueueUpdate.SendError(TransactionId(transactionId))
    is RoomSendQueueUpdate.SentEvent -> SendQueueUpdate.SentEvent(TransactionId(transactionId), EventId(eventId))
}
