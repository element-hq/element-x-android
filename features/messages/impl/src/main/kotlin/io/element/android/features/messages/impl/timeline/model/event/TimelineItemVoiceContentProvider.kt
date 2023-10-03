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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.collections.immutable.toPersistentList
import java.time.Duration

open class TimelineItemVoiceContentProvider : PreviewParameterProvider<TimelineItemVoiceContent> {
    override val values: Sequence<TimelineItemVoiceContent>
        get() = sequenceOf(
            aTimelineItemVoiceContent(body = "A sound.mp3"),
            aTimelineItemVoiceContent(body = "A bigger name sound.mp3"),
            aTimelineItemVoiceContent(body = "An even bigger bigger bigger bigger bigger bigger bigger sound name which doesn't fit .mp3"),
        )
}

fun aTimelineItemVoiceContent(
    eventId: String? = "\$anEventId",
    body: String = "A sound.mp3",
    durationMs: Long = 61_000,
    contentUri: String = "mxc://matrix.org/1234567890abcdefg",
    mimeType: String = MimeTypes.Ogg,
    waveform: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0),
) = TimelineItemVoiceContent(
    eventId = eventId?.let { EventId(it) },
    body = body,
    duration = Duration.ofMillis(durationMs),
    mediaSource = MediaSource(contentUri),
    mimeType = mimeType,
    waveform = waveform.toPersistentList(),
)
