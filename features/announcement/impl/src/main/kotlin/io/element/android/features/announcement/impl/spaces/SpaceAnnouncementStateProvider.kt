/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class SpaceAnnouncementStateProvider : PreviewParameterProvider<SpaceAnnouncementState> {
    override val values: Sequence<SpaceAnnouncementState>
        get() = sequenceOf(
            aSpaceAnnouncementState(),
        )
}

fun aSpaceAnnouncementState(
    eventSink: (SpaceAnnouncementEvents) -> Unit = {},
) = SpaceAnnouncementState(
    eventSink = eventSink,
)
