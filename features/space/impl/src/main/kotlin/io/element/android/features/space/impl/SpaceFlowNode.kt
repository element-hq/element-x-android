/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.space.impl

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.features.space.impl.addroom.AddRoomToSpaceNode
import io.element.android.features.space.impl.di.SpaceFlowGraph
import io.element.android.features.space.impl.leave.LeaveSpaceNode
import io.element.android.features.space.impl.root.SpaceNode
import io.element.android.features.space.impl.settings.SpaceSettingsFlowNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.spaces.SpaceService
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class SpaceFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    room: JoinedRoom,
    spaceService: SpaceService,
    graphFactory: SpaceFlowGraph.Factory,
) : BaseFlowNode<SpaceFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
), DependencyInjectionGraphOwner {
    private val callback: SpaceEntryPoint.Callback = callback()
    private val spaceRoomList = spaceService.spaceRoomList(room.roomId)
    override val graph = graphFactory.create(spaceRoomList)

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class Settings(val initialTarget: SpaceSettingsFlowNode.NavTarget = SpaceSettingsFlowNode.NavTarget.Root) : NavTarget

        @Parcelize
        data object Leave : NavTarget

        @Parcelize
        data object AddRoom : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onDestroy = {
                spaceRoomList.destroy()
            }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Leave -> {
                val callback = object : LeaveSpaceNode.Callback {
                    override fun closeLeaveSpaceFlow() {
                        backstack.pop()
                    }

                    override fun navigateToRolesAndPermissions() {
                        backstack.push(NavTarget.Settings(SpaceSettingsFlowNode.NavTarget.RolesAndPermissions))
                    }
                }
                createNode<LeaveSpaceNode>(buildContext, listOf(callback))
            }
            NavTarget.Root -> {
                val callback = object : SpaceNode.Callback {
                    override fun navigateToRoom(roomId: RoomId, viaParameters: List<String>) {
                        callback.navigateToRoom(roomId, viaParameters)
                    }

                    override fun navigateToSpaceSettings() {
                        backstack.push(NavTarget.Settings())
                    }

                    override fun navigateToRoomMemberList() {
                        callback.navigateToRoomMemberList()
                    }

                    override fun startLeaveSpaceFlow() {
                        backstack.push(NavTarget.Leave)
                    }

                    override fun navigateToAddRoom() {
                        backstack.push(NavTarget.AddRoom)
                    }
                }
                createNode<SpaceNode>(buildContext, listOf(callback))
            }
            is NavTarget.Settings -> {
                val callback = object : SpaceSettingsFlowNode.Callback {
                    override fun initialTarget() = navTarget.initialTarget

                    override fun navigateToSpaceMembers() {
                        callback.navigateToRoomMemberList()
                    }

                    override fun startLeaveSpaceFlow() {
                        backstack.push(NavTarget.Leave)
                    }

                    override fun closeSettings() {
                        backstack.pop()
                    }
                }
                createNode<SpaceSettingsFlowNode>(buildContext, listOf(callback))
            }
            NavTarget.AddRoom -> {
                val callback = object : AddRoomToSpaceNode.Callback {
                    override fun onFinish() {
                        backstack.pop()
                    }
                }
                createNode<AddRoomToSpaceNode>(buildContext, listOf(callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) = BackstackView()
}
