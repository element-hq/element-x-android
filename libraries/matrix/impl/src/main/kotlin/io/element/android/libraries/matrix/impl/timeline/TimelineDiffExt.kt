/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import uniffi.matrix_sdk_ui.EventItemOrigin

/**
 * Tries to get an event origin from the TimelineDiff.
 * If there is multiple events in the diff, uses the first one as it should be a good indicator.
 */
internal fun TimelineDiff.eventOrigin(): EventItemOrigin? {
    return when (change()) {
        TimelineChange.APPEND -> {
            append()?.firstOrNull()?.eventOrigin()
        }
        TimelineChange.PUSH_BACK -> {
            pushBack()?.eventOrigin()
        }
        TimelineChange.PUSH_FRONT -> {
            pushFront()?.eventOrigin()
        }
        TimelineChange.SET -> {
            set()?.item?.eventOrigin()
        }
        TimelineChange.INSERT -> {
            insert()?.item?.eventOrigin()
        }
        TimelineChange.RESET -> {
            reset()?.firstOrNull()?.eventOrigin()
        }
        else -> null
    }
}

private fun TimelineItem.eventOrigin(): EventItemOrigin? {
    return asEvent()?.origin
}
