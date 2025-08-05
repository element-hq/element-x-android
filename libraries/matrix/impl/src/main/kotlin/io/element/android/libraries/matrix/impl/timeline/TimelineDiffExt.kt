/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import uniffi.matrix_sdk_ui.EventItemOrigin

/**
 * Tries to get an event origin from the TimelineDiff.
 * If there is multiple events in the diff, uses the first one as it should be a good indicator.
 */
internal fun TimelineDiff.eventOrigin(): EventItemOrigin? {
    return when (this) {
        is TimelineDiff.Append -> values.firstOrNull()?.eventOrigin()
        is TimelineDiff.PushBack -> value.eventOrigin()
        is TimelineDiff.PushFront -> value.eventOrigin()
        is TimelineDiff.Set -> value.eventOrigin()
        is TimelineDiff.Insert -> value.eventOrigin()
        is TimelineDiff.Reset -> values.firstOrNull()?.eventOrigin()
        else -> null
    }
}

private fun TimelineItem.eventOrigin(): EventItemOrigin? {
    return asEvent()?.origin
}
