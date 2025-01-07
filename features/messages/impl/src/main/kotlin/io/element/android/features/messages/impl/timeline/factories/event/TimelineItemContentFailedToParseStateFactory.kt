/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import javax.inject.Inject

class TimelineItemContentFailedToParseStateFactory @Inject constructor() {
    @Suppress("UNUSED_PARAMETER")
    fun create(failedToParseState: FailedToParseStateContent): TimelineItemEventContent {
        return TimelineItemUnknownContent
    }
}
