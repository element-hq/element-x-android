/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.details

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.MediaInfo

sealed interface MediaBottomSheetState {
    data object Hidden : MediaBottomSheetState

    data class MediaDeleteConfirmationState(
        val eventId: EventId,
        val mediaInfo: MediaInfo,
        val thumbnailSource: MediaSource?,
    ) : MediaBottomSheetState

    data class MediaDetailsBottomSheetState(
        val eventId: EventId?,
        val canDelete: Boolean,
        val mediaInfo: MediaInfo,
        val thumbnailSource: MediaSource?,
    ) : MediaBottomSheetState
}
