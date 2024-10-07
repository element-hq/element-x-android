/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt

import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.ImmutableList

data class ReadReceiptViewState(
    val sendState: LocalEventSendState?,
    val isLastOutgoingMessage: Boolean,
    val receipts: ImmutableList<ReadReceiptData>,
)
