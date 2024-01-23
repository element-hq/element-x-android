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

package io.element.android.features.preferences.impl.notifications.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesNode(SessionScope::class)
class EditDefaultNotificationSettingNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: EditDefaultNotificationSettingPresenter.Factory
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openRoomNotificationSettings(roomId: RoomId)
    }

    data class Inputs(
        val isOneToOne: Boolean
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callbacks = plugins<Callback>()
    private val presenter = presenterFactory.create(inputs.isOneToOne)

    private fun openRoomNotificationSettings(roomId: RoomId) {
        callbacks.forEach { it.openRoomNotificationSettings(roomId) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        EditDefaultNotificationSettingView(
            state = state,
            openRoomNotificationSettings = { openRoomNotificationSettings(it) },
            onBackPressed = ::navigateUp,
            modifier = modifier,
        )
    }
}
