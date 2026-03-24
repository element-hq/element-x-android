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
            aTimelineItemLocationContent(mode = TimelineItemLocationContent.Mode.Live(isActive = true)),
            aTimelineItemLocationContent(mode = TimelineItemLocationContent.Mode.Live(isActive = false)),
        )
}

fun aTimelineItemLocationContent(
    body: String = "",
    senderId: UserId = UserId("@sender:matrix.org"),
    senderProfile: ProfileDetails = aProfileDetailsReady(),
    mode: TimelineItemLocationContent.Mode = TimelineItemLocationContent.Mode.Static,
) = TimelineItemLocationContent(
    body = body,
    location = Location(
        lat = 52.2445,
        lon = 0.7186,
        accuracy = 5000f,
    ),
    senderId = senderId,
    senderProfile = senderProfile,
    mode = mode
)
