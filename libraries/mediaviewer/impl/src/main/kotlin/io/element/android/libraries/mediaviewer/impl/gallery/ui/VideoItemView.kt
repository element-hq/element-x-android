/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoItemView(
    video: MediaItem.Video,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (LocalInspectionMode.current) {
        ElementTheme.colors.bgDecorative2
    } else {
        Color.Transparent
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(bgColor),
    ) {
        var isLoaded by remember { mutableStateOf(false) }
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isLoaded) Modifier.background(Color.White) else Modifier),
            model = video.thumbnailMediaRequestData,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            contentDescription = null,
            onState = { isLoaded = it is AsyncImagePainter.State.Success },
        )
        VideoInfoRow(
            video = video,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun VideoInfoRow(
    video: MediaItem.Video,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElementTheme.colors.bgCanvasDefault.copy(alpha = 0f),
                        ElementTheme.colors.bgCanvasDefault,
                    )
                )
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = CompoundIcons.VideoCallSolid(),
            contentDescription = null
        )
        if (video.duration != null) {
            Spacer(Modifier.weight(1f))
            Text(
                text = video.duration,
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textPrimary,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun VideoItemViewPreview(
    @PreviewParameter(MediaItemVideoProvider::class) video: MediaItem.Video,
) = ElementPreview {
    VideoItemView(
        video = video,
        onClick = {},
        onLongClick = {},
    )
}
