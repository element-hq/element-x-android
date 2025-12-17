/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.impl.di.SpaceFlowScope
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.JoinedRoom

@ContributesNode(SpaceFlowScope::class)
@AssistedInject
class LeaveSpaceNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    matrixClient: MatrixClient,
    room: JoinedRoom,
    presenterFactory: LeaveSpacePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun closeLeaveSpaceFlow()
        fun navigateToRolesAndPermissions()
    }

    private val leaveSpaceHandle = matrixClient.spaceService.getLeaveSpaceHandle(room.roomId)
    private val presenter: LeaveSpacePresenter = presenterFactory.create(leaveSpaceHandle)

    private val callback: Callback = callback()

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onDestroy = {
                leaveSpaceHandle.close()
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        LeaveSpaceView(
            state = state,
            onCancel = callback::closeLeaveSpaceFlow,
            onRolesAndPermissionsClick = callback::navigateToRolesAndPermissions,
            modifier = modifier
        )
    }
}
