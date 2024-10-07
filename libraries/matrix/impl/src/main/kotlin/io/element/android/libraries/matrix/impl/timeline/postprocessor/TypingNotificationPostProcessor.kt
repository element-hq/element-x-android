/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

/**
 * This post processor is responsible for adding a typing notification item to the timeline items when the timeline is in live mode.
 */
class TypingNotificationPostProcessor(private val mode: Timeline.Mode) {
    fun process(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        return if (mode == Timeline.Mode.LIVE) {
            buildList {
                addAll(items)
                add(
                    MatrixTimelineItem.Virtual(
                        uniqueId = UniqueId("TypingNotification"),
                        virtual = VirtualTimelineItem.TypingNotification
                    )
                )
            }
        } else {
            items
        }
    }
}
