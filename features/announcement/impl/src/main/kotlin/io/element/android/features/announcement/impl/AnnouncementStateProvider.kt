/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.announcement.api.Announcement

open class AnnouncementStateProvider : PreviewParameterProvider<AnnouncementState> {
    override val values: Sequence<AnnouncementState>
        get() = sequenceOf(
            anAnnouncementState(),
            anAnnouncementState(
                announcement = Announcement.Fullscreen.Space,
            ),
        )
}

fun anAnnouncementState(
    announcement: Announcement.Fullscreen? = null,
    eventSink: (AnnouncementEvent) -> Unit = {},
) = AnnouncementState(
    announcement = announcement,
    eventSink = eventSink,
)
