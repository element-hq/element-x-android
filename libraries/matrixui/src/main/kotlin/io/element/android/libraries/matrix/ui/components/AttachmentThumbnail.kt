/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.PinIcon
import io.element.android.libraries.designsystem.components.blurhash.BlurHashAsyncImage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import kotlinx.parcelize.Parcelize

@Composable
fun AttachmentThumbnail(
    info: AttachmentThumbnailInfo,
    modifier: Modifier = Modifier,
    thumbnailSize: Long = 32L,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
) {
    if (info.thumbnailSource != null) {
        val mediaRequestData = MediaRequestData(
            source = info.thumbnailSource,
            kind = MediaRequestData.Kind.Thumbnail(thumbnailSize),
        )
        BlurHashAsyncImage(
            model = mediaRequestData,
            blurHash = info.blurHash,
            contentDescription = info.textContent,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else if (info.blurHash != null) {
        BlurHashAsyncImage(
            model = null,
            blurHash = info.blurHash,
            contentDescription = info.textContent,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            when (info.type) {
                AttachmentThumbnailType.Image -> {
                    Icon(
                        imageVector = CompoundIcons.Image(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Video -> {
                    Icon(
                        imageVector = CompoundIcons.VideoCall(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Audio -> {
                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Voice -> {
                    Icon(
                        imageVector = CompoundIcons.MicOnSolid(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.File -> {
                    Icon(
                        imageVector = CompoundIcons.Attachment(),
                        contentDescription = info.textContent,
                        modifier = Modifier.rotate(-45f)
                    )
                }
                AttachmentThumbnailType.Location -> {
                    PinIcon(
                        modifier = Modifier.fillMaxSize()
                    )
                    /*
                    // For coherency across the app, we should us this instead. Waiting for design decision.
                    Icon(
                        resourceId = R.drawable.ic_september_location,
                        contentDescription = info.textContent,
                    )
                     */
                }
                AttachmentThumbnailType.Poll -> {
                    Icon(
                        imageVector = CompoundIcons.Polls(),
                        contentDescription = info.textContent,
                    )
                }
            }
        }
    }
}

@Parcelize
enum class AttachmentThumbnailType : Parcelable {
    Image,
    Video,
    File,
    Audio,
    Location,
    Voice,
    Poll,
}

@Parcelize
data class AttachmentThumbnailInfo(
    val type: AttachmentThumbnailType,
    val thumbnailSource: MediaSource? = null,
    val textContent: String? = null,
    val blurHash: String? = null,
) : Parcelable

@PreviewsDayNight
@Composable
internal fun AttachmentThumbnailPreview(@PreviewParameter(AttachmentThumbnailInfoProvider::class) data: AttachmentThumbnailInfo) = ElementPreview {
    AttachmentThumbnail(
        info = data,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(4.dp))
    )
}
