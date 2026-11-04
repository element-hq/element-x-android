/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt

import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ReadReceiptViewState(
    val sendState: LocalEventSendState?,
    val isLastOutgoingMessage: Boolean,
    private val renderReadReceipts: Boolean,
    private val knownReceipts: ImmutableList<ReadReceiptData>,
) {
    val receipts = if (renderReadReceipts) knownReceipts else persistentListOf()
}
