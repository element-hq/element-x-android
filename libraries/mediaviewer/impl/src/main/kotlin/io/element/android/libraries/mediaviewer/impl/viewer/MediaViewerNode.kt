/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.compound.theme.ForcedDarkElementTheme
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint

@ContributesNode(RoomScope::class)
class MediaViewerNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaViewerPresenter.Factory,
) : Node(buildContext, plugins = plugins),
    MediaViewerNavigator {
    private val inputs = inputs<MediaViewerEntryPoint.Params>()

    private fun onDone() {
        plugins<MediaViewerEntryPoint.Callback>().forEach {
            it.onDone()
        }
    }

    override fun onViewInTimelineClick(eventId: EventId) {
        plugins<MediaViewerEntryPoint.Callback>().forEach {
            it.onViewInTimeline(eventId)
        }
    }

    override fun onItemDeleted() {
        onDone()
    }

    private val presenter = presenterFactory.create(
        inputs = inputs,
        navigator = this,
    )

    @Composable
    override fun View(modifier: Modifier) {
        ForcedDarkElementTheme {
            val state = presenter.present()
            MediaViewerView(
                state = state,
                modifier = modifier,
                onBackClick = ::onDone
            )
        }
    }
}
