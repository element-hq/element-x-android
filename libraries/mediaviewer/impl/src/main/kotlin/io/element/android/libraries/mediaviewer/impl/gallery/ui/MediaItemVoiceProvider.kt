/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.preview.loremIpsum
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemVoice

class MediaItemVoiceProvider : PreviewParameterProvider<MediaItem.Voice> {
    override val values: Sequence<MediaItem.Voice>
        get() = sequenceOf(
            aMediaItemVoice(),
            aMediaItemVoice(
                filename = "A long filename that should be truncated.ogg",
                caption = "A caption",
            ),
            aMediaItemVoice(
                caption = loremIpsum,
            ),
            aMediaItemVoice(
                waveform = emptyList(),
            ),
        )
}
