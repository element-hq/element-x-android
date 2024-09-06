/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.virtual

import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import org.matrix.rustcomponents.sdk.VirtualTimelineItem as RustVirtualTimelineItem

class VirtualTimelineItemMapper {
    fun map(virtualTimelineItem: RustVirtualTimelineItem): VirtualTimelineItem {
        return when (virtualTimelineItem) {
            is RustVirtualTimelineItem.DayDivider -> VirtualTimelineItem.DayDivider(virtualTimelineItem.ts.toLong())
            RustVirtualTimelineItem.ReadMarker -> VirtualTimelineItem.ReadMarker
        }
    }
}
