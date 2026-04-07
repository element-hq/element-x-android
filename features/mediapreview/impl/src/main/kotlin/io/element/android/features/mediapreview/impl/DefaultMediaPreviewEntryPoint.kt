/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.mediapreview.api.MediaPreviewEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultMediaPreviewEntryPoint : MediaPreviewEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: MediaPreviewEntryPoint.Params,
        callback: MediaPreviewEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<MediaPreviewNode>(
            buildContext = buildContext,
            plugins = listOf(
                MediaPreviewNode.Inputs(
                    localMedia = params.localMedia,
                    config = params.config,
                    onSend = { caption, optimizeImage, videoPreset, onComplete ->
                        callback.onSend(caption, optimizeImage, videoPreset, onComplete)
                    },
                    onCancel = { callback.onCancel() },
                ),
                callback,
            )
        )
    }
}
