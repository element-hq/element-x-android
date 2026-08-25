/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local

import android.net.Uri
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.mediaviewer.api.MediaInfo

/**
 * Builds the [LocalMedia] the media viewer renders, from either a downloaded Matrix media file or an arbitrary content uri.
 */
interface LocalMediaFactory {
    /**
     * This method will create a [LocalMedia] with the given [MediaFile] and [MediaInfo].
     *
     * @param mediaFile the file already downloaded from the homeserver.
     * @param mediaInfo what is known about the media from the event that carried it.
     */
    fun createFromMediaFile(
        mediaFile: MediaFile,
        mediaInfo: MediaInfo,
    ): LocalMedia

    /**
     * This method will create a [LocalMedia] with the given mimeType, name and formattedFileSize
     * If any of those params are null, it'll try to read them from the content.
     *
     * @param uri the content to display, for instance one just picked by the user.
     * @param mimeType the MIME type, or `null` to read it from the content.
     * @param name the file name, or `null` to read it from the content.
     * @param formattedFileSize the size to display, or `null` to read it from the content.
     */
    fun createFromUri(
        uri: Uri,
        mimeType: String?,
        name: String?,
        formattedFileSize: String?
    ): LocalMedia
}
