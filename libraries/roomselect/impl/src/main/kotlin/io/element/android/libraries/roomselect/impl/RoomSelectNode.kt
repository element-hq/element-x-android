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

package io.element.android.libraries.roomselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode

@ContributesNode(SessionScope::class)
class RoomSelectNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: RoomSelectPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val mode: RoomSelectMode,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.mode)

    private val callbacks = plugins.filterIsInstance<RoomSelectEntryPoint.Callback>()

    private fun onDismiss() {
        callbacks.forEach { it.onCancel() }
    }

    private fun onSubmit(roomIds: List<RoomId>) {
        callbacks.forEach { it.onRoomSelected(roomIds) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RoomSelectView(
            state = state,
            onDismiss = ::onDismiss,
            onSubmit = ::onSubmit,
            modifier = modifier
        )
    }
}
