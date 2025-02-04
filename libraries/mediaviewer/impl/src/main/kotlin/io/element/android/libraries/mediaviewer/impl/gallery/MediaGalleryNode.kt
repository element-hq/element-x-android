/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.impl.gallery.di.LocalMediaItemPresenterFactories
import io.element.android.libraries.mediaviewer.impl.gallery.di.MediaItemPresenterFactories
import io.element.android.libraries.mediaviewer.impl.model.MediaItem

@ContributesNode(RoomScope::class)
class MediaGalleryNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaGalleryPresenter.Factory,
    private val mediaItemPresenterFactories: MediaItemPresenterFactories,
) : Node(buildContext, plugins = plugins),
    MediaGalleryNavigator {
    private val presenter = presenterFactory.create(
        navigator = this,
    )

    interface Callback : Plugin {
        fun onBackClick()
        fun onItemClick(item: MediaItem.Event)
        fun onViewInTimeline(eventId: EventId)
    }

    private fun onBackClick() {
        plugins<Callback>().forEach {
            it.onBackClick()
        }
    }

    override fun onViewInTimelineClick(eventId: EventId) {
        plugins<Callback>().forEach {
            it.onViewInTimeline(eventId)
        }
    }

    private fun onItemClick(item: MediaItem.Event) {
        plugins<Callback>().forEach {
            it.onItemClick(item)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        CompositionLocalProvider(
            LocalMediaItemPresenterFactories provides mediaItemPresenterFactories,
        ) {
            val state = presenter.present()
            MediaGalleryView(
                state = state,
                onBackClick = ::onBackClick,
                onItemClick = ::onItemClick,
                modifier = modifier,
            )
        }
    }
}
