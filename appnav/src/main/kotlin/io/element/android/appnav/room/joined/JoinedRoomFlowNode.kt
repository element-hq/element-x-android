/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.appnav.room.joined

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.transition.JumpToEndTransitionHandler
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.room.RoomNavigationTarget
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.ui.room.LoadingRoomState
import io.element.android.libraries.matrix.ui.room.LoadingRoomStateFlowFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class JoinedRoomFlowNode @AssistedInject constructor(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    loadingRoomStateFlowFactory: LoadingRoomStateFlowFactory,
    private val syncService: SyncService,
) :
    BaseFlowNode<JoinedRoomFlowNode.NavTarget>(
        backstack = BackStack(
            initialElement = NavTarget.Loading,
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
        plugins = plugins
    ) {
    data class Inputs(
        val roomId: RoomId,
        val initialElement: RoomNavigationTarget,
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
                val roomFlowNodeCallback = plugins<JoinedRoomLoadedFlowNode.Callback>()
                val awaitRoomState = loadingRoomStateStateFlow.value
                if (awaitRoomState is LoadingRoomState.Loaded) {
                    val inputs = JoinedRoomLoadedFlowNode.Inputs(
                        room = awaitRoomState.room,
                        initialElement = inputs.initialElement
                    )
                    createNode<JoinedRoomLoadedFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback)
                } else {
                    loadingNode(buildContext, this::navigateUp)
                }
            }
            NavTarget.Loading -> {
                loadingNode(buildContext, this::navigateUp)
            }
        }
    }

    private fun loadingNode(buildContext: BuildContext, onBackClick: () -> Unit) = node(buildContext) { modifier ->
        val loadingRoomState by loadingRoomStateStateFlow.collectAsState()
        val isOnline by syncService.isOnline.collectAsState()
        LoadingRoomNodeView(
            state = loadingRoomState,
            hasNetworkConnection = isOnline,
            modifier = modifier,
            onBackClick = onBackClick
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(
            transitionHandler = JumpToEndTransitionHandler(),
        )
    }
}
