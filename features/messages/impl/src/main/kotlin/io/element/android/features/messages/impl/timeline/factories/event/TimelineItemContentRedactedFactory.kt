/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import javax.inject.Inject

class TimelineItemContentRedactedFactory @Inject constructor() {
    fun create(@Suppress("UNUSED_PARAMETER") content: RedactedContent): TimelineItemEventContent {
        return TimelineItemRedactedContent
    }
}
