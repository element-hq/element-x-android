/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.local.audio.MediaAudioView
import io.element.android.libraries.mediaviewer.impl.local.file.MediaFileView
import io.element.android.libraries.mediaviewer.impl.local.image.MediaImageView
import io.element.android.libraries.mediaviewer.impl.local.pdf.MediaPdfView
import io.element.android.libraries.mediaviewer.impl.local.video.MediaVideoView

@Composable
fun LocalMediaView(
    localMedia: LocalMedia?,
    bottomPaddingInPixels: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDisplayed: Boolean = true,
    localMediaViewState: LocalMediaViewState = rememberLocalMediaViewState(),
    mediaInfo: MediaInfo? = localMedia?.info,
) {
    val mimeType = mediaInfo?.mimeType
    when {
        mimeType.isMimeTypeImage() -> MediaImageView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
            onClick = onClick,
        )
        mimeType.isMimeTypeVideo() -> MediaVideoView(
            isDisplayed = isDisplayed,
            localMediaViewState = localMediaViewState,
            bottomPaddingInPixels = bottomPaddingInPixels,
            localMedia = localMedia,
            modifier = modifier,
        )
        mimeType == MimeTypes.Pdf -> MediaPdfView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
            onClick = onClick,
        )
        mimeType.isMimeTypeAudio() -> MediaAudioView(
            isDisplayed = isDisplayed,
            localMediaViewState = localMediaViewState,
            bottomPaddingInPixels = bottomPaddingInPixels,
            localMedia = localMedia,
            info = mediaInfo,
            modifier = modifier,
        )
        else -> MediaFileView(
            localMediaViewState = localMediaViewState,
            uri = localMedia?.uri,
            info = mediaInfo,
            modifier = modifier,
            onClick = onClick,
        )
    }
}
