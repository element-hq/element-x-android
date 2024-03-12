/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@ContributesNode(RoomScope::class)
class RolesAndPermissionsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RolesAndPermissionsPresenter,
    private val room: MatrixRoom,
) : Node(buildContext, plugins = plugins), RoomDetailsAdminSettingsNavigator {
    interface Callback : Plugin {
        fun openAdminList()
        fun openModeratorList()
    }

    private val callback = plugins<Callback>().first()

    override fun onBackPressed() = navigateUp()
    override fun openAdminList() = callback.openAdminList()
    override fun openModeratorList() = callback.openModeratorList()

    override fun onBuilt() {
        super.onBuilt()

        // Reload members when the user sees this screen
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    lifecycleScope.launch { room.updateMembers() }
                }
            }
        })

        // If the user is not an admin anymore, exit this section since they won't have permissions to use it
        lifecycleScope.launch {
            room.membersStateFlow
                .map { state ->
                    state.roomMembers().orEmpty().find { it.userId == room.sessionId }
                }
                .filter { it?.role != RoomMember.Role.ADMIN }
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
            roomDetailsAdminSettingsNavigator = this,
            modifier = modifier,
        )
    }
}

interface RoomDetailsAdminSettingsNavigator {
    fun onBackPressed() {}
    fun openAdminList() {}
    fun openModeratorList() {}
}
