/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

internal fun List<MatrixTimelineItem>.hasEncryptionHistoryBanner(): Boolean {
    val firstItem = firstOrNull()
    return firstItem is MatrixTimelineItem.Virtual &&
        firstItem.virtual is VirtualTimelineItem.EncryptedHistoryBanner
}
