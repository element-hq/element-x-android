/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.MediaInfo

interface MediaGalleryEntryPoint : FeatureEntryPoint {
    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface NodeBuilder {
        fun callback(callback: Callback): NodeBuilder
        fun params(params: Params): NodeBuilder
        fun build(): Node
    }

    interface Callback : Plugin {
        fun onDone()
        fun onItemClick(
            eventId: EventId?,
            mediaInfo: MediaInfo,
            mediaSource: MediaSource,
            thumbnailSource: MediaSource?,
        )
    }

    data class Params(
        val dummy: Boolean,
    ) : NodeInputs
}
