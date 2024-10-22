/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.RoomScope

// CHANGE THE SCOPE
@ContributesNode(RoomScope::class)
class MapRealtimePresenterNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: MapRealtimePresenterPresenter,
) : Node(buildContext, plugins = plugins) {

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        MapRealtimeView(
            state = state,
            onBackPressed = ::navigateUp,
            onMessagesPressed = TODO(),
            onJoinCallClick = TODO(), // TODO(tb): This isnt used since we are using the view directly in MessagesView and not creating a new screen
            isCallOngoing = false,
        )
    }
}
