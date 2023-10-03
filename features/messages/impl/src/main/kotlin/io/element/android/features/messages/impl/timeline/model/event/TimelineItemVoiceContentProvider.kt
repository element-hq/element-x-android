/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.collections.immutable.persistentListOf

open class TimelineItemVoiceContentProvider : PreviewParameterProvider<TimelineItemAudioContent> {
    override val values: Sequence<TimelineItemAudioContent>
        get() = sequenceOf(
            aTimelineItemAudioContent("A sound.mp3"),
            aTimelineItemAudioContent("A bigger name sound.mp3"),
            aTimelineItemAudioContent("An even bigger bigger bigger bigger bigger bigger bigger sound name which doesn't fit .mp3"),
        )
}

fun aTimelineItemVoiceContent(
    fileName: String = "A sound.mp3",
) = TimelineItemVoiceContent(
    body = fileName,
    mimeType = MimeTypes.Pdf,
    formattedFileSize = "100kB",
    fileExtension = "mp3",
    duration = 100,
    audioSource = MediaSource(""),
    waveform = persistentListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0),
)
