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

package io.element.android.appnav

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class AwaitRoomNode @AssistedInject constructor(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val matrixClient: MatrixClient,
) :
    BackstackNode<AwaitRoomNode.NavTarget>(
        backstack = BackStack(
            initialElement = NavTarget.Loading,
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
        plugins = plugins
    ) {

    data class Inputs(
        val roomId: RoomId,
        val initialElement: RoomFlowNode.NavTarget = RoomFlowNode.NavTarget.Messages,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val roomStateFlow = suspend {
        matrixClient.getRoom(roomId = inputs.roomId)
    }
        .asFlow()
        .stateIn(lifecycleScope, SharingStarted.Eagerly, null)

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Loading : NavTarget

        @Parcelize
        object Loaded : NavTarget
    }

    init {
        roomStateFlow.onEach { room ->
            if (room == null) {
                backstack.safeRoot(NavTarget.Loading)
            } else {
                backstack.safeRoot(NavTarget.Loaded)
            }
        }.launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Loaded -> {
                val nodeLifecycleCallbacks = plugins<NodeLifecycleCallback>()
                val roomFlowNodeCallback = plugins<RoomFlowNode.Callback>()
                val room = roomStateFlow.value
                if (room == null) {
                    loadingNode(buildContext)
                } else {
                    val inputs = RoomFlowNode.Inputs(room, initialElement = inputs.initialElement)
                    createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback + nodeLifecycleCallbacks)
                }
            }
            NavTarget.Loading -> {
                loadingNode(buildContext)
            }
        }
    }

    private fun loadingNode(buildContext: BuildContext) = node(buildContext) {
        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
        )
    }
}

