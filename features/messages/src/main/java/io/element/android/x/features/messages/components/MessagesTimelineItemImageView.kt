package io.element.android.x.features.messages.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import io.element.android.x.features.messages.model.content.MessagesTimelineItemImageContent

@Composable
fun MessagesTimelineItemImageView(
    content: MessagesTimelineItemImageContent,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        AsyncImage(
            model = content.imageMeta,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}