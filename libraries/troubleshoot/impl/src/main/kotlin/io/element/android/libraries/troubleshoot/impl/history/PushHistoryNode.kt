/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint
import io.element.android.services.analytics.api.ScreenTracker

@ContributesNode(SessionScope::class)
class PushHistoryNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PushHistoryPresenter,
    private val screenTracker: ScreenTracker,
) : Node(buildContext, plugins = plugins) {
    private fun onDone() {
        plugins<PushHistoryEntryPoint.Callback>().forEach {
            it.onDone()
        }
    }

    private fun onItemClick(sessionId: SessionId, roomId: RoomId, eventId: EventId) {
        plugins<PushHistoryEntryPoint.Callback>().forEach {
            it.onItemClick(sessionId, roomId, eventId)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        screenTracker.TrackScreen(MobileScreen.ScreenName.NotificationTroubleshoot)
        val state = presenter.present()
        PushHistoryView(
            state = state,
            onBackClick = ::onDone,
            onItemClick = ::onItemClick,
            modifier = modifier,
        )
    }
}
