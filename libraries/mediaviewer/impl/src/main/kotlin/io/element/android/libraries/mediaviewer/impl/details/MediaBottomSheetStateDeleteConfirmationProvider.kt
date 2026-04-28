/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.details

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo

open class MediaBottomSheetStateDeleteConfirmationProvider : PreviewParameterProvider<MediaBottomSheetState.DeleteConfirmation> {
    override val values: Sequence<MediaBottomSheetState.DeleteConfirmation>
        get() = sequenceOf(
            aMediaBottomSheetStateDeleteConfirmation(),
            aMediaBottomSheetStateDeleteConfirmation(
                thumbnailSource = MediaSource("url_thumbnail")
            ),
        )
}

fun aMediaBottomSheetStateDeleteConfirmation(
    mediaInfo: MediaInfo = anImageMediaInfo(
        senderName = "Alice",
    ),
    thumbnailSource: MediaSource? = null,
) = MediaBottomSheetState.DeleteConfirmation(
    eventId = EventId("\$eventId"),
    mediaInfo = mediaInfo,
    thumbnailSource = thumbnailSource,
)
