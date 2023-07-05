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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import io.element.android.appnav.NodeLifecycleCallback
import io.element.android.appnav.safeRoot
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.theme.placeholderBackground
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.theme.ElementTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class AwaitRoomNode @AssistedInject constructor(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    awaitRoomStateFlowFactory: AwaitRoomStateFlowFactory,
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
    private val awaitRoomStateFlow = awaitRoomStateFlowFactory.create(lifecycleScope, inputs.roomId)

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Loading : NavTarget

        @Parcelize
        object Loaded : NavTarget
    }

    init {
        awaitRoomStateFlow.onEach { awaitRoomState ->
            when (awaitRoomState) {
                is AwaitRoomState.Loaded -> backstack.safeRoot(NavTarget.Loaded)
                else -> backstack.safeRoot(NavTarget.Loading)
            }
        }.launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Loaded -> {
                val nodeLifecycleCallbacks = plugins<NodeLifecycleCallback>()
                val roomFlowNodeCallback = plugins<RoomFlowNode.Callback>()
                val awaitRoomState = awaitRoomStateFlow.value
                if (awaitRoomState is AwaitRoomState.Loaded) {
                    val inputs = RoomFlowNode.Inputs(awaitRoomState.room, initialElement = inputs.initialElement)
                    createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback + nodeLifecycleCallbacks)
                } else {
                    loadingNode(buildContext, this::navigateUp)
                }
            }
            NavTarget.Loading -> {
                loadingNode(buildContext, this::navigateUp)
            }
        }
    }

    private fun loadingNode(buildContext: BuildContext, onBackPressed: () -> Unit) = node(buildContext) { modifier ->
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                TopAppBar(
                    modifier = Modifier,
                    navigationIcon = {
                        BackButton(onClick = onBackPressed)
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(AvatarSize.TimelineRoom.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(color = ElementTheme.colors.placeholderBackground, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PlaceholderAtom(width = 20.dp, height = 7.dp)
                            Spacer(modifier = Modifier.width(7.dp))
                            PlaceholderAtom(width = 45.dp, height = 7.dp)
                        }
                    },
                )
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
        )

    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
        )
    }
}

