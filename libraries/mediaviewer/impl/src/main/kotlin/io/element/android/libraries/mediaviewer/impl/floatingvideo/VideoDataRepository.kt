/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo

import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData

class VideoDataRepository {
    companion object {
        @Volatile
        private var INSTANCE: VideoDataRepository? = null

        fun getInstance(): VideoDataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VideoDataRepository().also { INSTANCE = it }
            }
        }
    }

    private val videoDataMap = mutableMapOf<String, MediaViewerPageData.MediaViewerData>()

    fun storeVideoData(videoId: String, data: MediaViewerPageData.MediaViewerData) {
        videoDataMap[videoId] = data
    }

    fun getVideoData(videoId: String): MediaViewerPageData.MediaViewerData? {
        return videoDataMap[videoId]
    }

    fun removeVideoData(videoId: String) {
        videoDataMap.remove(videoId)
    }

    fun clear() {
        videoDataMap.clear()
    }
}

