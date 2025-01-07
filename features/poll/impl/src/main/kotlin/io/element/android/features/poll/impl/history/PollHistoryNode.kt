/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

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
import io.element.android.libraries.matrix.api.core.EventId

@ContributesNode(RoomScope::class)
class PollHistoryNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PollHistoryPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun onEditPoll(pollStartEventId: EventId)
    }

    private fun onEditPoll(pollStartEventId: EventId) {
        plugins<Callback>().forEach { it.onEditPoll(pollStartEventId) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        PollHistoryView(
            state = presenter.present(),
            modifier = modifier,
            onEditPoll = ::onEditPoll,
            goBack = this::navigateUp,
        )
    }
}
