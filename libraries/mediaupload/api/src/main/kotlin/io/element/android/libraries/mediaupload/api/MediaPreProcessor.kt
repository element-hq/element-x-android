/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri

interface MediaPreProcessor {
    /**
     * Given a [uri] and [mimeType], pre-processes the media before it's uploaded, resizing, transcoding, and removing sensitive info from its metadata.
     * If [deleteOriginal] is `true`, the file reference by the [uri] will be automatically deleted too when this process finishes.
     * @return a [Result] with the [MediaUploadInfo] containing all the info needed to begin the upload.
     */
    suspend fun process(
        uri: Uri,
        mimeType: String,
        deleteOriginal: Boolean,
        compressIfPossible: Boolean,
    ): Result<MediaUploadInfo>

    data class Failure(override val cause: Throwable?) : Exception(cause)
}
