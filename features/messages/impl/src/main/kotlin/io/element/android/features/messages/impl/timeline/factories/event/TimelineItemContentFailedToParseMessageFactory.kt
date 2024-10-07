/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import javax.inject.Inject

class TimelineItemContentFailedToParseMessageFactory @Inject constructor() {
    fun create(@Suppress("UNUSED_PARAMETER") failedToParseMessageLike: FailedToParseMessageLikeContent): TimelineItemEventContent {
        return TimelineItemUnknownContent
    }
}
