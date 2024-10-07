/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContentProvider
import io.element.android.features.messages.impl.timeline.protection.ProtectedView
import io.element.android.libraries.designsystem.components.blurhash.blurHashBackground
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.ui.strings.CommonStrings

private const val STICKER_SIZE_IN_DP = 128

@Composable
fun TimelineItemStickerView(
    content: TimelineItemStickerContent,
    hideMediaContent: Boolean,
    onShowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = content.body.takeIf { it.isNotEmpty() } ?: stringResource(CommonStrings.common_image)
    Column(
        modifier = modifier.semantics { contentDescription = description },
    ) {
        TimelineItemAspectRatioBox(
            modifier = Modifier.blurHashBackground(content.blurhash, alpha = 0.9f),
            aspectRatio = content.aspectRatio,
            minHeight = STICKER_SIZE_IN_DP,
            maxHeight = STICKER_SIZE_IN_DP,
        ) {
            ProtectedView(
                hideContent = hideMediaContent,
                onShowClick = onShowClick,
            ) {
                var isLoaded by remember { mutableStateOf(false) }
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isLoaded) Modifier.background(Color.White) else Modifier),
                    model = MediaRequestData(
                        source = content.preferredMediaSource,
                        kind = MediaRequestData.Kind.File(
                            body = content.body,
                            mimeType = content.mimeType,
                        ),
                    ),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                    contentDescription = description,
                    onState = { isLoaded = it is AsyncImagePainter.State.Success },
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemStickerViewPreview(@PreviewParameter(TimelineItemStickerContentProvider::class) content: TimelineItemStickerContent) = ElementPreview {
    TimelineItemStickerView(
        content = content,
        hideMediaContent = false,
        onShowClick = {},
    )
}
