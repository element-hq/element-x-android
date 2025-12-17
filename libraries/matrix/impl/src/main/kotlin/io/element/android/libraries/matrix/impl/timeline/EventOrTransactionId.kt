/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import org.matrix.rustcomponents.sdk.EventOrTransactionId as RustEventOrTransactionId

fun EventOrTransactionId.toRustEventOrTransactionId() = when (this) {
    is EventOrTransactionId.Event -> RustEventOrTransactionId.EventId(id.value)
    is EventOrTransactionId.Transaction -> RustEventOrTransactionId.TransactionId(id.value)
}
