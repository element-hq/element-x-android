/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages.reply

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

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
                type = VideoMessageType("Video", null, null, MediaSource("url"), null),
            ),
            aMessageContent(
                body = "Audio",
                type = AudioMessageType("Audio", null, null, MediaSource("url"), null),
            ),
            aMessageContent(
                body = "Voice",
                type = VoiceMessageType("Voice", null, null, MediaSource("url"), null, null),
            ),
            aMessageContent(
                body = "Image",
                type = ImageMessageType("Image", null, null, MediaSource("url"), null),
            ),
            aMessageContent(
                body = "Sticker",
                type = StickerMessageType("Image", null, null, MediaSource("url"), null),
            ),
            aMessageContent(
                body = "File",
                type = FileMessageType("File", null, null, MediaSource("url"), null),
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
}

class InReplyToDetailsDisambiguatedProvider : InReplyToDetailsProvider() {
    override val values: Sequence<InReplyToDetails>
        get() = sequenceOf(
            aMessageContent(
                body = "Message which are being replied.",
                type = TextMessageType("Message which are being replied.", null)
            ),
        ).map {
            aInReplyToDetails(
                displayNameAmbiguous = true,
                eventContent = it,
            )
        }
}

class InReplyToDetailsInformativeProvider : InReplyToDetailsProvider() {
    override val values: Sequence<InReplyToDetails>
        get() = sequenceOf(
            RedactedContent,
            UnableToDecryptContent(UnableToDecryptContent.Data.Unknown),
        ).map {
            aInReplyToDetails(
                eventContent = it,
            )
        }
}

class InReplyToDetailsOtherProvider : InReplyToDetailsProvider() {
    override val values: Sequence<InReplyToDetails>
        get() = sequenceOf(
            InReplyToDetails.Loading(eventId = EventId("\$anEventId")),
            InReplyToDetails.Error(eventId = EventId("\$anEventId"), message = "An error message."),
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

private fun aInReplyToDetails(
    eventContent: EventContent,
    displayNameAmbiguous: Boolean = false,
) = InReplyToDetails.Ready(
    eventId = EventId("\$event"),
    eventContent = eventContent,
    senderId = UserId("@Sender:domain"),
    senderProfile = aProfileTimelineDetailsReady(
        displayNameAmbiguous = displayNameAmbiguous,
    ),
    textContent = (eventContent as? MessageContent)?.body.orEmpty(),
)

fun aProfileTimelineDetailsReady(
    displayName: String? = "Sender",
    displayNameAmbiguous: Boolean = false,
    avatarUrl: String? = null,
) = ProfileTimelineDetails.Ready(
    displayName = displayName,
    displayNameAmbiguous = displayNameAmbiguous,
    avatarUrl = avatarUrl,
)
