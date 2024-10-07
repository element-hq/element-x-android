/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ForcedDarkElementTheme
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.local.MediaInfo

@ContributesNode(RoomScope::class)
open class MediaViewerNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaViewerPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val mediaInfo: MediaInfo,
        val mediaSource: MediaSource,
        val thumbnailSource: MediaSource?,
        val canDownload: Boolean,
        val canShare: Boolean,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    private val presenter = presenterFactory.create(inputs)

    @Composable
    override fun View(modifier: Modifier) {
        ForcedDarkElementTheme {
            val state = presenter.present()
            MediaViewerView(
                state = state,
                modifier = modifier,
                onBackClick = this::navigateUp
            )
        }
    }
}
