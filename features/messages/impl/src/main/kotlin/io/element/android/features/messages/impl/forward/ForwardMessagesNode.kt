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

package io.element.android.features.messages.impl.forward

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
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList

@ContributesNode(RoomScope::class)
class ForwardMessagesNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ForwardMessagesPresenter.Factory,
) : Node(buildContext, plugins = plugins) {

    interface Callback : Plugin {
        fun onForwardedToSingleRoom(roomId: RoomId)
    }

    data class Inputs(val eventId: EventId) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val presenter = presenterFactory.create(inputs.eventId.value)
    private val callbacks = plugins.filterIsInstance<Callback>()

    private fun onSucceeded(roomIds: ImmutableList<RoomId>) {
        navigateUp()
        if (roomIds.size == 1) {
            val targetRoomId = roomIds.first()
            callbacks.forEach { it.onForwardedToSingleRoom(targetRoomId) }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ForwardMessagesView(
            state = state,
            onDismiss = ::navigateUp,
            onForwardingSucceeded = ::onSucceeded,
            modifier = modifier
        )
    }
}
