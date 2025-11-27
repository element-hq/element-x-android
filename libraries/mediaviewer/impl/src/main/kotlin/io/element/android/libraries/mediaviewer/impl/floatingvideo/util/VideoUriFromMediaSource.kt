/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.util


import android.net.Uri
import androidx.core.net.toUri
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import timber.log.Timber
import java.io.File

fun MediaSource.getUri(): Uri {
    return try {
        when {
            url.startsWith("http://") || url.startsWith("https://") -> {
                // Remote URL
                url.toUri()
            }
            url.startsWith("file://") -> {
                // Already a file URI
                url.toUri()
            }
            url.startsWith("/") -> {
                // Local file path, convert to file URI
                File(url).toUri()
            }
            url.startsWith("content://") -> {
                // Content URI (from MediaStore, etc.)
                url.toUri()
            }
            else -> {
                // Try parsing as-is, might work
                url.toUri()
            }
        }
    } catch (e: Exception) {
        Timber.tag("Uri Parsing").e(e)
        Uri.EMPTY
    }
}

//    currentVideoData?.let { data ->
//        resolvedUri = when (val downloadedState = data.downloadedMedia.value) {
//            is AsyncData.Success -> downloadedState.data.uri
//            else -> data.mediaSource.getUri()
//        }
//    }
//MediaViewerPageData.MediaViewerData?
fun MediaViewerPageData.MediaViewerData?.getUri(): Uri {
    return this?.let { data ->
        when (val downloadedState = data.downloadedMedia.value) {
            is AsyncData.Success -> downloadedState.data.uri
            else -> data.mediaSource.getUri()
        }
    } ?: Uri.EMPTY
}
