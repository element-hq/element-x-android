/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@Inject
class SpaceNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SpacePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val inputs = plugins.filterIsInstance<SpaceEntryPoint.Inputs>().single()
    private val callback = plugins.filterIsInstance<SpaceEntryPoint.Callback>().single()
    private val presenter = presenterFactory.create(inputs)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        SpaceView(
            state = state,
            onBackClick = ::navigateUp,
            onRoomClick = { roomId ->
                callback.onOpenRoom(roomId)
            },
            modifier = modifier
        )
    }
}
