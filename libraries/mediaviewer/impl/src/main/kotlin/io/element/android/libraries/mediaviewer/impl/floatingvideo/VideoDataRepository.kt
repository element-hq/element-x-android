/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.mediaviewer.impl.viewer.MediaViewerPageData
import java.util.UUID

@SingleIn(AppScope::class)
@Inject
class VideoDataRepository {

    private val videoDataMap = mutableMapOf<String, MediaViewerPageData.MediaViewerData>()

    fun storeVideoData(data: MediaViewerPageData.MediaViewerData) : String{
        val id = UUID.randomUUID().toString()
        videoDataMap[id] = data
        return id
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

