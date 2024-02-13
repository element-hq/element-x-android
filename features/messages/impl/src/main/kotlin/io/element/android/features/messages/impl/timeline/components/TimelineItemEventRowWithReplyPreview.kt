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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.InReplyToDetails
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithReplyPreview(
    @PreviewParameter(InReplyToDetailsProvider::class) inReplyToDetails: InReplyToDetails,
) = ElementPreview {
    TimelineItemEventRowWithReplyContentToPreview(inReplyToDetails)
}

@Composable
internal fun TimelineItemEventRowWithReplyContentToPreview(inReplyToDetails: InReplyToDetails) {
    Column {
        sequenceOf(false, true).forEach {
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    timelineItemReactions = aTimelineItemReactions(count = 0),
                    content = aTimelineItemTextContent().copy(
                        body = "A reply."
                    ),
                    inReplyTo = inReplyToDetails,
                    groupPosition = TimelineItemGroupPosition.First,
                ),
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    timelineItemReactions = aTimelineItemReactions(count = 0),
                    content = aTimelineItemImageContent().copy(
                        aspectRatio = 2.5f
                    ),
                    inReplyTo = inReplyToDetails,
                    isThreaded = true,
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
            )
        }
    }
}

open class InReplyToDetailsProvider : PreviewParameterProvider<InReplyToDetails> {
    override val values: Sequence<InReplyToDetails>
        get() = sequenceOf(
            aMessageContent(
                body = "Message which are being replied.",
                type = TextMessageType("Message which are being replied.", null)
            ),
            aMessageContent(
                body = "Message which are being replied, and which was long enough to be displayed on two lines (only!).",
                type = TextMessageType("Message which are being replied, and which was long enough to be displayed on two lines (only!).", null)
            ),
            aMessageContent(
                body = "Video",
                type = VideoMessageType("Video", MediaSource("url"), null),
            ),
            aMessageContent(
                body = "Audio",
                type = AudioMessageType("Audio", MediaSource("url"), null),
            ),
            aMessageContent(
                body = "Voice",
                type = VoiceMessageType("Voice", MediaSource("url"), null, null),
            ),
            aMessageContent(
                body = "Image",
                type = ImageMessageType("Image", MediaSource("url"), null),
            ),
            aMessageContent(
                body = "Sticker",
                type = StickerMessageType("Image", MediaSource("url"), null),
            ),
            aMessageContent(
                body = "File",
                type = FileMessageType("File", MediaSource("url"), null),
            ),
            aMessageContent(
                body = "Location",
                type = LocationMessageType("Location", "geo:1,2", null),
            ),
            aMessageContent(
                body = "Notice",
                type = NoticeMessageType("Notice", null),
            ),
            aMessageContent(
                body = "Emote",
                type = EmoteMessageType("Emote", null),
            ),
            PollContent(
                question = "Poll which are being replied.",
                kind = PollKind.Disclosed,
                maxSelections = 1u,
                answers = persistentListOf(),
                votes = persistentMapOf(),
                endTime = null,
                isEdited = false,
            ),
        ).map {
            aInReplyToDetails(
                eventContent = it,
            )
        }

    private fun aMessageContent(
        body: String,
        type: MessageType,
    ) = MessageContent(
        body = body,
        inReplyTo = null,
        isEdited = false,
        isThreaded = false,
        type = type,
    )

    protected fun aInReplyToDetails(
        eventContent: EventContent,
    ) = InReplyToDetails(
        eventId = EventId("\$event"),
        eventContent = eventContent,
        senderId = UserId("@Sender:domain"),
        senderDisplayName = "Sender",
        senderAvatarUrl = null,
        textContent = (eventContent as? MessageContent)?.body.orEmpty(),
    )
}
