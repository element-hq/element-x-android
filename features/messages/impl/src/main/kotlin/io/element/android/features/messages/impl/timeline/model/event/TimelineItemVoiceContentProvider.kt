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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

open class TimelineItemVoiceContentProvider : PreviewParameterProvider<TimelineItemVoiceContent> {
    override val values: Sequence<TimelineItemVoiceContent>
        get() = sequenceOf(
            aTimelineItemVoiceContent(
                duration = 1.milliseconds,
                waveform = listOf(),
            ),
            aTimelineItemVoiceContent(
                duration = 10_000.milliseconds,
                waveform = listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f, 0f),
            ),
            aTimelineItemVoiceContent(
                duration = 30.minutes,
                waveform = List(1024) { it / 1024f },
            ),
        )
}

fun aTimelineItemVoiceContent(
    eventId: String? = "\$anEventId",
    body: String = "body doesn't really matter for a voice message",
    duration: Duration = 61_000.milliseconds,
    contentUri: String = "mxc://matrix.org/1234567890abcdefg",
    mimeType: String = MimeTypes.Ogg,
    waveform: List<Float> = listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f, 0f),
) = TimelineItemVoiceContent(
    eventId = eventId?.let { EventId(it) },
    body = body,
    duration = duration,
    mediaSource = MediaSource(contentUri),
    mimeType = mimeType,
    waveform = waveform.toPersistentList(),
)
