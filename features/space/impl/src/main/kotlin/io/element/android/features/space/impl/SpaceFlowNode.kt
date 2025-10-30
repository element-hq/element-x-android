/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.features.space.impl.di.SpaceFlowGraph
import io.element.android.features.space.impl.leave.LeaveSpaceNode
import io.element.android.features.space.impl.root.SpaceNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceService
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class SpaceFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
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
    private val inputs: SpaceEntryPoint.Inputs = inputs()
    private val callback = plugins.filterIsInstance<SpaceEntryPoint.Callback>().single()
    private val spaceRoomList = spaceService.spaceRoomList(inputs.roomId)
    override val graph = graphFactory.create(spaceRoomList)

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object Leave : NavTarget
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
                createNode<LeaveSpaceNode>(buildContext, listOf(inputs))
            }
            NavTarget.Root -> {
                val callback = object : SpaceNode.Callback {
                    override fun onOpenRoom(roomId: RoomId, viaParameters: List<String>) {
                        callback.onOpenRoom(roomId, viaParameters)
                    }

                    override fun onOpenDetails() {
                        callback.onOpenDetails()
                    }

                    override fun onOpenMemberList() {
                        callback.onOpenMemberList()
                    }

                    override fun onLeaveSpace() {
                        backstack.push(NavTarget.Leave)
                    }
                }
                createNode<SpaceNode>(buildContext, listOf(inputs, callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) = BackstackView()
}
