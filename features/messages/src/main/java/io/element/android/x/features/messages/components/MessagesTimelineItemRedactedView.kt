package io.element.android.x.features.messages.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.x.features.messages.model.content.MessagesTimelineItemRedactedContent

@Composable
fun MessagesTimelineItemRedactedView(
    content: MessagesTimelineItemRedactedContent,
    modifier: Modifier = Modifier
) {
    MessagesTimelineItemInformativeView(
        text = "This message has been deleted",
        iconDescription = "Delete",
        icon = Icons.Default.Delete,
        modifier = modifier
    )
}