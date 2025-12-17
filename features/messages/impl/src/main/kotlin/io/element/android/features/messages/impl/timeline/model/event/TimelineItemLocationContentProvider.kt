/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.location.api.Location

open class TimelineItemLocationContentProvider : PreviewParameterProvider<TimelineItemLocationContent> {
    override val values: Sequence<TimelineItemLocationContent>
        get() = sequenceOf(
            aTimelineItemLocationContent(),
            aTimelineItemLocationContent("This is a description!"),
        )
}

fun aTimelineItemLocationContent(description: String? = null) = TimelineItemLocationContent(
    body = "User location geo:52.2445,0.7186;u=5000",
    location = Location(
        lat = 52.2445,
        lon = 0.7186,
        accuracy = 5000f,
    ),
    description = description,
)
