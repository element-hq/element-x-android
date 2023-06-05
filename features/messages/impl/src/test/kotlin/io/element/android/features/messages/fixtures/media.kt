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

package io.element.android.features.messages.fixtures

import android.net.Uri
import androidx.media3.common.MimeTypes
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.features.messages.impl.media.local.MediaInfo

fun aLocalMedia(
    uri: Uri,
    mimeType: String = MimeTypes.IMAGE_JPEG,
    name: String = "a media",
    size: Long = 1000,
) = LocalMedia(
    uri = uri,
    info = MediaInfo(
        mimeType = mimeType,
        name = name,
        formattedFileSize = "${size}B",
    )
)

fun aMediaAttachment(localMedia: LocalMedia, compressIfPossible: Boolean = true) = Attachment.Media(
    localMedia = localMedia,
    compressIfPossible = compressIfPossible,
)

