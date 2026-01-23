/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.impl.di.SpaceFlowScope
import io.element.android.libraries.architecture.appyx.launchMolecule
import io.element.android.libraries.architecture.callback

@ContributesNode(SpaceFlowScope::class)
@AssistedInject
class AddRoomToSpaceNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AddRoomToSpacePresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onFinish()
    }

    private val callback: Callback = callback()
    private val stateFlow = launchMolecule { presenter.present() }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        AddRoomToSpaceView(
            state = state,
            onBackClick = ::navigateUp,
            onRoomsAdded = callback::onFinish,
            modifier = modifier
        )
    }
}
