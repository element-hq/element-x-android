/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.details

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo

open class MediaBottomSheetStateDetailsProvider : PreviewParameterProvider<MediaBottomSheetState.Details> {
    override val values: Sequence<MediaBottomSheetState.Details>
        get() = sequenceOf(
            aMediaBottomSheetStateDetails(),
            aMediaBottomSheetStateDetails(
                canDelete = false,
            ),
            aMediaBottomSheetStateDetails(
                mediaInfo = anApkMediaInfo(
                    dateSentFull = "December 6, 2024 at 12:59",
                ),
            ),
            aMediaBottomSheetStateDetails(
                eventId = null,
            ),
        )
}

fun aMediaBottomSheetStateDetails(
    eventId: EventId? = EventId($$"$eventId"),
    canDelete: Boolean = true,
    mediaInfo: MediaInfo = anImageMediaInfo(
        senderName = "Alice",
        dateSentFull = "December 6, 2024 at 12:59",
    ),
) = MediaBottomSheetState.Details(
    eventId = eventId,
    canDelete = canDelete,
    mediaInfo = mediaInfo,
    thumbnailSource = null,
)
