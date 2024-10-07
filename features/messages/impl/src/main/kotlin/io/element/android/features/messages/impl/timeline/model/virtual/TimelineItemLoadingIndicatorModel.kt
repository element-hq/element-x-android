/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.virtual

import io.element.android.libraries.matrix.api.timeline.Timeline

data class TimelineItemLoadingIndicatorModel(
    val direction: Timeline.PaginationDirection,
    val timestamp: Long,
) : TimelineItemVirtualModel {
    override val type: String = "TimelineItemLoadingIndicatorModel"
}
