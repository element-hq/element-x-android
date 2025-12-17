/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.preview.loremIpsum
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemFile

class MediaItemFileProvider : PreviewParameterProvider<MediaItem.File> {
    override val values: Sequence<MediaItem.File>
        get() = sequenceOf(
            aMediaItemFile(),
            aMediaItemFile(
                filename = "A long filename that should be truncated.jpg",
                caption = "A caption",
            ),
            aMediaItemFile(
                caption = loremIpsum,
            ),
        )
}
