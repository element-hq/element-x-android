/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline

sealed interface MediaViewerEvents {
    data class LoadMedia(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvents
    data class SaveOnDisk(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvents
    data class Share(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvents
    data class OpenWith(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvents
    data class ClearLoadingError(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvents
    data class ViewInTimeline(val eventId: EventId) : MediaViewerEvents
    data class Forward(val eventId: EventId) : MediaViewerEvents
    data class OpenInfo(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvents
    data class ConfirmDelete(
        val eventId: EventId,
        val data: MediaViewerPageData.MediaViewerData,
    ) : MediaViewerEvents

    data object CloseBottomSheet : MediaViewerEvents
    data class Delete(val eventId: EventId) : MediaViewerEvents
    data class OnNavigateTo(val index: Int) : MediaViewerEvents
    data class LoadMore(val direction: Timeline.PaginationDirection) : MediaViewerEvents
}
