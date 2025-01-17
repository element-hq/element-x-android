/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.preview.loremIpsum
import io.element.android.libraries.designsystem.components.media.aWaveForm
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.aVoiceMediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

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

fun aMediaItemVoice(
    id: UniqueId = UniqueId("fileId"),
    filename: String = "filename.ogg",
    caption: String? = null,
    duration: String? = "1:23",
    waveform: List<Float> = aWaveForm(),
): MediaItem.Voice {
    return MediaItem.Voice(
        id = id,
        eventId = null,
        mediaInfo = aVoiceMediaInfo(
            filename = filename,
            caption = caption,
            duration = duration,
            waveForm = waveform,
        ),
        mediaSource = MediaSource(""),
    )
}
