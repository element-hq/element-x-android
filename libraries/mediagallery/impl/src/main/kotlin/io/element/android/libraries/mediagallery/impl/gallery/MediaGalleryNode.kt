/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.impl.gallery

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.mediagallery.api.MediaGalleryEntryPoint

@ContributesNode(RoomScope::class)
class MediaGalleryNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: MediaGalleryPresenter,
) : Node(buildContext, plugins = plugins) {

    private fun onDone() {
        plugins<MediaGalleryEntryPoint.Callback>().forEach {
            it.onDone()
        }
    }

    private fun onItemClick(item: MediaItem.Event) {
        plugins<MediaGalleryEntryPoint.Callback>().forEach {
            it.onItemClick(
                item.eventId(),
                item.mediaInfo(),
                item.mediaSource(),
                item.thumbnailSource(),
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        MediaGalleryView(
            state = state,
            onBackClick = ::onDone,
            onItemClick = ::onItemClick,
            modifier = modifier,
        )
    }
}
