/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.bubble

import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition

data class BubbleState(
    val groupPosition: TimelineItemGroupPosition,
    val isMine: Boolean,
    val isHighlighted: Boolean,
    val timelineRoomInfo: TimelineRoomInfo,
) {
    /** True to cut out the top start corner of the bubble, to give margin for the sender avatar. */
    val cutTopStart: Boolean = groupPosition.isNew() && !isMine && !timelineRoomInfo.isDm
}
