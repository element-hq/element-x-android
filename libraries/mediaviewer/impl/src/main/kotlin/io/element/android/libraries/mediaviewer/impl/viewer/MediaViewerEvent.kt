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

sealed interface MediaViewerEvent {
    data class LoadMedia(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvent
    data class SaveOnDisk(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvent
    data class Share(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvent
    data class OpenWith(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvent
    data class ClearLoadingError(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvent
    data class ViewInTimeline(val eventId: EventId) : MediaViewerEvent
    data class Forward(val eventId: EventId) : MediaViewerEvent
    data class OpenInfo(val data: MediaViewerPageData.MediaViewerData) : MediaViewerEvent
    data class ConfirmDelete(
        val eventId: EventId,
        val data: MediaViewerPageData.MediaViewerData,
    ) : MediaViewerEvent

    data object CloseBottomSheet : MediaViewerEvent
    data class Delete(val eventId: EventId) : MediaViewerEvent
    data class OnNavigateTo(val index: Int) : MediaViewerEvent
    data class LoadMore(val direction: Timeline.PaginationDirection) : MediaViewerEvent
}
