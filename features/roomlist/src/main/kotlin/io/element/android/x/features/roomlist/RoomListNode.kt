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

package io.element.android.x.features.roomlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.presenterConnector
import io.element.android.x.di.SessionScope
import io.element.android.x.matrix.core.RoomId

@ContributesNode(SessionScope::class)
class RoomListNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenter: RoomListPresenter,
) : Node(buildContext, plugins = plugins) {

    interface Callback : Plugin {
        fun onRoomClicked(roomId: RoomId)
        fun onSettingsClicked()
    }

    private val connector = presenterConnector(presenter)

    private fun onRoomClicked(roomId: RoomId) {
        plugins<Callback>().forEach { it.onRoomClicked(roomId) }
    }

    private fun onOpenSettings() {
        plugins<Callback>().forEach { it.onSettingsClicked() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by connector.stateFlow.collectAsState()
        RoomListView(
            state = state,
            onRoomClicked = this::onRoomClicked,
            onOpenSettings = this::onOpenSettings
        )
    }
}
