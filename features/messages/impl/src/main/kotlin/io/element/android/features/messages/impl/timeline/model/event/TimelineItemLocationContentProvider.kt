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
            aTimelineItemLocationContent(
                mode = aStaticLocationMode()
            ),
            aTimelineItemLocationContent(
                mode = aLiveLocationMode(isActive = true)
            ),
            aTimelineItemLocationContent(
                mode = aLiveLocationMode(isActive = true, lastKnownLocation = null)
            ),
            aTimelineItemLocationContent(
                mode = aLiveLocationMode(isActive = true, isOwnUser = false)
            ),
            aTimelineItemLocationContent(
                mode = aLiveLocationMode(isActive = false)
            ),
        )
}
fun aLiveLocationMode(
    isActive: Boolean,
    isOwnUser: Boolean = true,
    lastKnownLocation: Location? = aLocation(),
    endsAt: String = "Ends at 12:34",
    endTimestamp: Long = 0L,
): TimelineItemLocationContent.Mode = TimelineItemLocationContent.Mode.Live(
    isActive = isActive,
    endsAt = endsAt,
    endTimestamp = endTimestamp,
    isOwnUser = isOwnUser,
    lastKnownLocation = lastKnownLocation
)

fun aStaticLocationMode(location: Location = aLocation()) = TimelineItemLocationContent.Mode.Static(location)

fun aTimelineItemLocationContent(
    senderId: UserId = UserId("@sender:matrix.org"),
    senderProfile: ProfileDetails = aProfileDetailsReady(),
    description: String? = null,
    mode: TimelineItemLocationContent.Mode,
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
