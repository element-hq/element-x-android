package io.element.android.x.features.messages.model.content

import org.matrix.rustcomponents.sdk.EncryptedMessage

data class MessagesTimelineItemEncryptedContent(
    val encryptedMessage: EncryptedMessage
) : MessagesTimelineItemContent