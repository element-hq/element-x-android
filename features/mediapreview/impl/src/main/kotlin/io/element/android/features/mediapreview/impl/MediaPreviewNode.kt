/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.mediapreview.api.MediaPreviewConfig
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaRenderer
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset

@ContributesNode(SessionScope::class)
@AssistedInject
class MediaPreviewNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaPreviewPresenter.Factory,
    private val localMediaRenderer: LocalMediaRenderer,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val localMedia: LocalMedia,
        val config: MediaPreviewConfig,
        val onSend: (String?, Boolean, VideoCompressionPreset?, () -> Unit) -> Unit,
        val onCancel: () -> Unit,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    private val presenter = presenterFactory.create(
        localMedia = inputs.localMedia,
        config = inputs.config,
        onSendListener = object : MediaPreviewPresenter.OnSendListener {
            override fun onSend(
                caption: String?,
                optimizeImage: Boolean,
                videoPreset: VideoCompressionPreset?,
                onComplete: () -> Unit,
            ) {
                inputs.onSend(caption, optimizeImage, videoPreset, onComplete)
            }
        },
        onCancelListener = object : MediaPreviewPresenter.OnCancelListener {
            override fun onCancel() {
                inputs.onCancel()
            }
        },
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        MediaPreviewView(
            state = state,
            localMediaRenderer = localMediaRenderer,
            modifier = modifier,
        )
    }
}
