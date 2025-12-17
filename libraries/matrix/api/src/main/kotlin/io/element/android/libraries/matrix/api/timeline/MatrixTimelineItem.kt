/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

sealed interface MatrixTimelineItem {
    data class Event(val uniqueId: UniqueId, val event: EventTimelineItem) : MatrixTimelineItem {
        val eventId: EventId? = event.eventId
        val transactionId: TransactionId? = event.transactionId
    }

    data class Virtual(val uniqueId: UniqueId, val virtual: VirtualTimelineItem) : MatrixTimelineItem
    data object Other : MatrixTimelineItem
}
