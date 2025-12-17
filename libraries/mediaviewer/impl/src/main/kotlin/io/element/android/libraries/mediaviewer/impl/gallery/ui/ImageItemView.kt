/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ImageItemView(
    image: MediaItem.Image,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
            )
            .onKeyboardContextMenuAction(onLongClick),
    ) {
        var isLoaded by remember { mutableStateOf(false) }
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isLoaded) Modifier.background(Color.White) else Modifier),
            model = image.thumbnailMediaRequestData,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            contentDescription = null,
            onState = { isLoaded = it is AsyncImagePainter.State.Success },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ImageItemViewPreview() = ElementPreview {
    ImageItemView(
        image = aMediaItemImage(),
        onClick = {},
        onLongClick = {},
    )
}
