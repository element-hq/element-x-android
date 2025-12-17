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
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemAudio

class MediaItemAudioProvider : PreviewParameterProvider<MediaItem.Audio> {
    override val values: Sequence<MediaItem.Audio>
        get() = sequenceOf(
            aMediaItemAudio(),
            aMediaItemAudio(
                filename = "A long filename that should be truncated.mp3",
                caption = "A caption",
            ),
            aMediaItemAudio(
                caption = loremIpsum,
            ),
        )
}
