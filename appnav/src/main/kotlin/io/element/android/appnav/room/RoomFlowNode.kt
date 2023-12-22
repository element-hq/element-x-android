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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.appnav.room

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class RoomFlowNode @AssistedInject constructor(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    loadingRoomStateFlowFactory: LoadingRoomStateFlowFactory,
    private val networkMonitor: NetworkMonitor,
) :
    BaseFlowNode<RoomFlowNode.NavTarget>(
        backstack = BackStack(
            initialElement = NavTarget.Loading,
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
        plugins = plugins
    ) {

    data class Inputs(
        val roomId: RoomId,
        val initialElement: RoomLoadedFlowNode.NavTarget = RoomLoadedFlowNode.NavTarget.Messages,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val loadingRoomStateStateFlow = loadingRoomStateFlowFactory.create(lifecycleScope, inputs.roomId)

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Loading : NavTarget

        @Parcelize
        data object Loaded : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()
        loadingRoomStateStateFlow
            .map {
                it is LoadingRoomState.Loaded
            }
            .distinctUntilChanged()
            .onEach { isLoaded ->
                if (isLoaded) {
                    backstack.newRoot(NavTarget.Loaded)
                } else {
                    backstack.newRoot(NavTarget.Loading)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Loaded -> {
                val roomFlowNodeCallback = plugins<RoomLoadedFlowNode.Callback>()
                val awaitRoomState = loadingRoomStateStateFlow.value
                if (awaitRoomState is LoadingRoomState.Loaded) {
                    val inputs = RoomLoadedFlowNode.Inputs(awaitRoomState.room, initialElement = inputs.initialElement)
                    createNode<RoomLoadedFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback)
                } else {
                    loadingNode(buildContext, this::navigateUp)
                }
            }
            NavTarget.Loading -> {
                loadingNode(buildContext, this::navigateUp)
            }
        }
    }

    private fun loadingNode(buildContext: BuildContext, onBackClicked: () -> Unit) = node(buildContext) { modifier ->
        val loadingRoomState by loadingRoomStateStateFlow.collectAsState()
        val networkStatus by networkMonitor.connectivity.collectAsState()
        LoadingRoomNodeView(
            state = loadingRoomState,
            hasNetworkConnection = networkStatus == NetworkStatus.Online,
            modifier = modifier,
            onBackClicked = onBackClicked
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}

