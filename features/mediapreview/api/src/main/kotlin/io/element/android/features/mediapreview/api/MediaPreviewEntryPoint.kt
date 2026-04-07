/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

interface MediaPreviewEntryPoint : FeatureEntryPoint {
    data class Params(
        val localMedia: LocalMedia,
        val config: MediaPreviewConfig = MediaPreviewConfig(),
    )

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        fun onSend(
            caption: String?,
            optimizeImage: Boolean,
            videoPreset: VideoCompressionPreset?,
            onComplete: () -> Unit,
        )
        fun onCancel()
    }
}

data class MediaPreviewConfig(
    val initialCaption: String? = null,
    val showProgressDialog: Boolean = false,
    val sendMode: SendMode = SendMode.DIRECT,
    val timelineMode: io.element.android.libraries.matrix.api.timeline.Timeline.Mode = io.element.android.libraries.matrix.api.timeline.Timeline.Mode.Live,
    val inReplyToEventId: io.element.android.libraries.matrix.api.core.EventId? = null,
    val joinedRoom: io.element.android.libraries.matrix.api.room.JoinedRoom? = null,
)

enum class SendMode {
    DIRECT,
    PREPROCESS,
}
