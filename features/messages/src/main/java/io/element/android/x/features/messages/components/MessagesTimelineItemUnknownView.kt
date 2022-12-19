package io.element.android.x.features.messages.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.x.features.messages.model.content.MessagesTimelineItemUnknownContent

@Composable
fun MessagesTimelineItemUnknownView(
    content: MessagesTimelineItemUnknownContent,
    modifier: Modifier = Modifier
) {
    MessagesTimelineItemInformativeView(
        text = "Event not handled by EAX",
        iconDescription = "Info",
        icon = Icons.Default.Info,
        modifier = modifier
    )
}
