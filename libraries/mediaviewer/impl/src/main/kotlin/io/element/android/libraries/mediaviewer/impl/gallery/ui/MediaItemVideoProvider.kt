/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemVideo

class MediaItemVideoProvider : PreviewParameterProvider<MediaItem.Video> {
    override val values: Sequence<MediaItem.Video>
        get() = sequenceOf(
            aMediaItemVideo(),
            aMediaItemVideo(
                duration = null,
            ),
        )
}
