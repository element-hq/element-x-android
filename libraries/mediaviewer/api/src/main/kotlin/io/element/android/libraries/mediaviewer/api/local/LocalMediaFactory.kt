/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local

import android.net.Uri
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.mediaviewer.api.MediaInfo

interface LocalMediaFactory {
    /**
     * This method will create a [LocalMedia] with the given [MediaFile] and [MediaInfo].
     */
    fun createFromMediaFile(
        mediaFile: MediaFile,
        mediaInfo: MediaInfo,
    ): LocalMedia

    /**
     * This method will create a [LocalMedia] with the given mimeType, name and formattedFileSize
     * If any of those params are null, it'll try to read them from the content.
     */
    fun createFromUri(
        uri: Uri,
        mimeType: String?,
        name: String?,
        formattedFileSize: String?
    ): LocalMedia
}
