/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.parcelize.Parcelize

interface MediaViewerEntryPoint : FeatureEntryPoint {
    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface NodeBuilder {
        fun callback(callback: Callback): NodeBuilder
        fun params(params: Params): NodeBuilder
        fun avatar(filename: String, avatarUrl: String): NodeBuilder
        fun build(): Node
    }

    interface Callback : Plugin {
        fun onDone()
        fun onViewInTimeline(eventId: EventId)
    }

    data class Params(
        val mode: MediaViewerMode,
        val eventId: EventId?,
        val mediaInfo: MediaInfo,
        val mediaSource: MediaSource,
        val thumbnailSource: MediaSource?,
        val canShowInfo: Boolean,
    ) : NodeInputs

    sealed interface MediaViewerMode : Parcelable {
        @Parcelize
        data object SingleMedia : MediaViewerMode

        @Parcelize
        data class TimelineImagesAndVideos(val timelineMode: Timeline.Mode) : MediaViewerMode

        @Parcelize
        data class TimelineFilesAndAudios(val timelineMode: Timeline.Mode) : MediaViewerMode
    }
}
