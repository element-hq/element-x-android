/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@ContributesNode(RoomScope::class)
class RolesAndPermissionsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RolesAndPermissionsPresenter,
    private val room: BaseRoom,
) : Node(buildContext, plugins = plugins), RolesAndPermissionsNavigator {
    interface Callback : Plugin, RolesAndPermissionsNavigator {
        override fun openAdminList()
        override fun openModeratorList()
        override fun openEditRoomDetailsPermissions()
        override fun openMessagesAndContentPermissions()
        override fun openModerationPermissions()
        override fun onBackClick() {}
    }

    private val callback = plugins<Callback>().first()

    @Stable
    private val navigator = object : RolesAndPermissionsNavigator by callback {
        override fun onBackClick() {
            navigateUp()
        }
    }

    override fun onBuilt() {
        super.onBuilt()

        // If the user is not an admin anymore, exit this section since they won't have permissions to use it
        lifecycleScope.launch {
            room.roomInfoFlow
                .filter { info ->
                    info.userPowerLevels[room.sessionId] != RoomMember.Role.ADMIN.powerLevel
                }
                .take(1)
                .onEach { navigateUp() }
                .collect()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RolesAndPermissionsView(
            state = state,
            rolesAndPermissionsNavigator = navigator,
            modifier = modifier,
        )
    }
}

interface RolesAndPermissionsNavigator {
    fun onBackClick() {}
    fun openAdminList() {}
    fun openModeratorList() {}
    fun openEditRoomDetailsPermissions() {}
    fun openMessagesAndContentPermissions() {}
    fun openModerationPermissions() {}
}
