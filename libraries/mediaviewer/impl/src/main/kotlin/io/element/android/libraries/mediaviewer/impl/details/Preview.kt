/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.details

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo

fun aMediaDetailsBottomSheetState(): MediaBottomSheetState.MediaDetailsBottomSheetState {
    return MediaBottomSheetState.MediaDetailsBottomSheetState(
        eventId = EventId("\$eventId"),
        canDelete = true,
        mediaInfo = anImageMediaInfo(
            senderName = "Alice",
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
