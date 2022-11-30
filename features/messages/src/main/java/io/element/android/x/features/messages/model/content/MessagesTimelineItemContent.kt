package io.element.android.x.features.messages.model.content

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.matrix.rustcomponents.sdk.EncryptedMessage
import org.matrix.rustcomponents.sdk.FormattedBody
import org.matrix.rustcomponents.sdk.MessageFormat

sealed interface MessagesTimelineItemContent

class MessagesTimelineItemContentProvider : PreviewParameterProvider<MessagesTimelineItemContent> {
    override val values = sequenceOf(
        MessagesTimelineItemEmoteContent(
            body = "Emote",
            htmlDocument = null
        ),
        MessagesTimelineItemEncryptedContent(
            encryptedMessage = EncryptedMessage.Unknown
        ),
        // TODO MessagesTimelineItemImageContent(),
        MessagesTimelineItemNoticeContent(
            body = "Notice",
            htmlDocument = null
        ),
        MessagesTimelineItemRedactedContent,
        MessagesTimelineItemTextContent(
            body = "Text",
            htmlDocument = null
        ),
        MessagesTimelineItemUnknownContent,
    )
}