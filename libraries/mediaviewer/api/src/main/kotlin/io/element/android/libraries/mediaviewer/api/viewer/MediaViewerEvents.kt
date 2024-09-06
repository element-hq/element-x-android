/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.viewer

sealed interface MediaViewerEvents {
    data object SaveOnDisk : MediaViewerEvents
    data object Share : MediaViewerEvents
    data object OpenWith : MediaViewerEvents
    data object RetryLoading : MediaViewerEvents
    data object ClearLoadingError : MediaViewerEvents
}
