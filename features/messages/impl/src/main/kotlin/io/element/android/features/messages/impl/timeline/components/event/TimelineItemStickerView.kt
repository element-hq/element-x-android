/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContentProvider
import io.element.android.features.messages.impl.timeline.protection.ProtectedView
import io.element.android.features.messages.impl.timeline.protection.coerceRatioWhenHidingContent
import io.element.android.libraries.designsystem.components.blurhash.blurHashBackground
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.ui.strings.CommonStrings

private const val STICKER_SIZE_IN_DP = 128

@Composable
fun TimelineItemStickerView(
    content: TimelineItemStickerContent,
    hideMediaContent: Boolean,
    onContentClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onShowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = content.bestDescription.takeIf { it.isNotEmpty() } ?: stringResource(CommonStrings.common_image)
    Column(
        modifier = modifier.semantics { contentDescription = description },
    ) {
        TimelineItemAspectRatioBox(
            modifier = Modifier.blurHashBackground(content.blurhash, alpha = 0.9f),
            aspectRatio = coerceRatioWhenHidingContent(content.aspectRatio, hideMediaContent),
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
                        .then(if (isLoaded) Modifier.background(Color.White) else Modifier)
                        .then(
                            if (onContentClick != null) {
                                Modifier
                                    .combinedClickable(
                                        onClick = onContentClick,
                                        onLongClick = onLongClick,
                                        onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
                                    )
                                    .onKeyboardContextMenuAction(onLongClick)
                            } else {
                                Modifier
                            }
                        ),
                    model = MediaRequestData(
                        source = content.preferredMediaSource,
                        kind = MediaRequestData.Kind.File(
                            fileName = content.filename,
                            mimeType = content.mimeType,
                        ),
                    ),
                    contentScale = ContentScale.Crop,
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
        onContentClick = {},
        onLongClick = {},
        onShowClick = {},
    )
}
