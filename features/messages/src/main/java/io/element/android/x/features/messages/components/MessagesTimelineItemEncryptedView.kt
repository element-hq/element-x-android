package io.element.android.x.features.messages.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.x.features.messages.model.content.MessagesTimelineItemEncryptedContent

@Composable
fun MessagesTimelineItemEncryptedView(
    content: MessagesTimelineItemEncryptedContent,
    modifier: Modifier = Modifier
) {
    MessagesTimelineItemInformativeView(
        text = "Decryption error",
        iconDescription = "Warning",
        icon = Icons.Default.Warning,
        modifier = modifier
    )
}
