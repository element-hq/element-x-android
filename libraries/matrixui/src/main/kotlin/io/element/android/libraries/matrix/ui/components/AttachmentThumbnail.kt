/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.ui.components

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.VideoCameraBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.element.android.libraries.designsystem.components.BlurHashAsyncImage
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
    if (info.mediaSource != null) {
        val mediaRequestData = MediaRequestData(
            source = info.mediaSource,
            kind = MediaRequestData.Kind.Thumbnail(thumbnailSize),
        )
        BlurHashAsyncImage(
            model = mediaRequestData,
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
                AttachmentThumbnailType.Video -> {
                    Icon(
                        imageVector = Icons.Outlined.VideoCameraBack,
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.File -> {
                    Icon(
                        imageVector = Icons.Outlined.Attachment,
                        contentDescription = info.textContent,
                        modifier = Modifier.rotate(-45f)
                    )
                }
                else -> Unit
            }
        }
    }
}

@Parcelize
enum class AttachmentThumbnailType: Parcelable {
    Image, Video, File
}

@Parcelize
data class AttachmentThumbnailInfo(
    val mediaSource: MediaSource?,
    val textContent: String?,
    val type: AttachmentThumbnailType?,
    val blurHash: String?,
): Parcelable
