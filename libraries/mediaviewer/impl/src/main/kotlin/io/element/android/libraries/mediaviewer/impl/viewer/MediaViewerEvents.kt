/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import io.element.android.libraries.matrix.api.core.EventId

sealed interface MediaViewerEvents {
    data object SaveOnDisk : MediaViewerEvents
    data object Share : MediaViewerEvents
    data object OpenWith : MediaViewerEvents
    data object RetryLoading : MediaViewerEvents
    data object ClearLoadingError : MediaViewerEvents
    data class ViewInTimeline(val eventId: EventId) : MediaViewerEvents
    data object OpenInfo : MediaViewerEvents
    data class ConfirmDelete(val eventId: EventId) : MediaViewerEvents
    data object CloseBottomSheet : MediaViewerEvents
    data class Delete(val eventId: EventId) : MediaViewerEvents
}
