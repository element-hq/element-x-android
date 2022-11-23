@file:OptIn(ExperimentalFoundationApi::class)

package io.element.android.x.features.messages.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.element.android.x.features.messages.model.content.MessagesTimelineItemImageContent

@Composable
fun MessagesTimelineItemImageView(
    content: MessagesTimelineItemImageContent,
    modifier: Modifier = Modifier
) {
    val widthPercent = if(content.aspectRatio > 1f){
        1f
    }else {
        0.7f
    }
    Box(
        modifier = modifier
            .fillMaxWidth(widthPercent)
            .aspectRatio(content.aspectRatio),
        contentAlignment = Alignment.Center,
    ) {

        var isLoading = rememberSaveable(content.imageMeta) { mutableStateOf(true) }
        val context = LocalContext.current
        val model = ImageRequest.Builder(context)
            .data(content.imageMeta)
            .build()

        AsyncImage(
            model = model,
            contentDescription = null,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            onSuccess = { isLoading.value = false },
        )
    }
}