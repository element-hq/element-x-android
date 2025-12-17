/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import org.matrix.rustcomponents.sdk.EventOrTransactionId as RustEventOrTransactionId

fun RustEventOrTransactionId.map(): EventOrTransactionId = when (this) {
    is RustEventOrTransactionId.EventId -> EventOrTransactionId.Event(EventId(eventId))
    is RustEventOrTransactionId.TransactionId -> EventOrTransactionId.Transaction(TransactionId(transactionId))
}
