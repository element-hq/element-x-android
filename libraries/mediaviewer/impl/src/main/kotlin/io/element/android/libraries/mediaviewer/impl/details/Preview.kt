/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.details

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo

fun aMediaDetailsBottomSheetState(
    dateSentFull: String = "December 6, 2024 at 12:59",
    canDelete: Boolean = true,
): MediaBottomSheetState.MediaDetailsBottomSheetState {
    return MediaBottomSheetState.MediaDetailsBottomSheetState(
        eventId = EventId("\$eventId"),
        canDelete = canDelete,
        mediaInfo = anImageMediaInfo(
            senderName = "Alice",
            dateSentFull = dateSentFull,
        ),
        thumbnailSource = null,
    )
}

fun aMediaDeleteConfirmationState(): MediaBottomSheetState.MediaDeleteConfirmationState {
    return MediaBottomSheetState.MediaDeleteConfirmationState(
        eventId = EventId("\$eventId"),
        mediaInfo = anImageMediaInfo(
            senderName = "Alice",
        ),
        thumbnailSource = null,
    )
}
