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
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.ui.messages.reply.aProfileDetailsReady

open class TimelineItemLocationContentProvider : PreviewParameterProvider<TimelineItemLocationContent> {
    override val values: Sequence<TimelineItemLocationContent>
        get() = sequenceOf(
            aTimelineItemLocationContent(),
            aTimelineItemLocationContent(
                mode = TimelineItemLocationContent.Mode.Live(
                    isActive = true,
                    endsAt = "Ends at 12:34",
                    endTimestamp = 0L,
                    canStop = true,
                    lastKnownLocation = aLocation()
                ),
            ),
            aTimelineItemLocationContent(
                mode = TimelineItemLocationContent.Mode.Live(
                    isActive = true,
                    endsAt = "Ends at 12:34",
                    endTimestamp = 0L,
                    lastKnownLocation = aLocation()
                ),
            ),
            aTimelineItemLocationContent(
                mode = TimelineItemLocationContent.Mode.Live(
                    isActive = true,
                    endsAt = "Ends at 12:34",
                    endTimestamp = 0L,
                    lastKnownLocation = null
                ),
            ),
            aTimelineItemLocationContent(
                mode = TimelineItemLocationContent.Mode.Live(
                    isActive = false,
                    endsAt = "",
                    endTimestamp = 0L,
                    lastKnownLocation = aLocation()
                ),
            ),
        )
}

fun aTimelineItemLocationContent(
    senderId: UserId = UserId("@sender:matrix.org"),
    senderProfile: ProfileDetails = aProfileDetailsReady(),
    description: String? = null,
    mode: TimelineItemLocationContent.Mode = TimelineItemLocationContent.Mode.Static(aLocation()),
) = TimelineItemLocationContent(
    senderId = senderId,
    senderProfile = senderProfile,
    description = description,
    mode = mode,
)

fun aLocation() = Location(
    lat = 52.2445,
    lon = 0.7186,
    accuracy = 5000f,
)
