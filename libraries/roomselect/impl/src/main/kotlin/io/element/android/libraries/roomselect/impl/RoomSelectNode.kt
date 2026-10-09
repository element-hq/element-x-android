/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.appyx.launchMolecule
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode

@ContributesNode(AppScope::class)
@AssistedInject
class RoomSelectNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: RoomSelectPresenter.Factory,
) : Node(buildContext, plugins = plugins), RoomSelectNavigator {
    data class Inputs(
        val sessionId: SessionId,
        val mode: RoomSelectMode,
        val maxNumberOfRooms: Int,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val callback: RoomSelectEntryPoint.Callback = callback()
    private val presenter = presenterFactory.create(
        initialSessionId = inputs.sessionId,
        mode = inputs.mode,
        maxNumberOfRooms = inputs.maxNumberOfRooms,
        navigator = this,
    )
    private val stateFlow = launchMolecule { presenter.present() }

    override fun navigateToSessionVerification(sessionId: SessionId) {
        callback.onSessionVerificationRequired(sessionId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        RoomSelectView(
            state = state,
            onDismiss = callback::onCancel,
            onSubmit = { roomIds -> callback.onRoomSelected(state.selectedAccount.userId, roomIds) },
            modifier = modifier
        )
    }
}
