/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.di.RoomComponentFactory
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@Inject
class ChangeRoomMemberRolesRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    roomComponentFactory: RoomComponentFactory,
) : ParentNode<ChangeRoomMemberRolesRootNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
), DependencyInjectionGraphOwner, ChangeRoomMemberRolesEntryPoint.NodeProxy {
    @Parcelize object NavTarget : Parcelable

    data class Inputs(
        val joinedRoom: JoinedRoom,
        val listType: ChangeRoomMemberRolesListType,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    override val graph = roomComponentFactory.create(inputs.joinedRoom)

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return createNode<ChangeRolesNode>(
            buildContext = buildContext,
            plugins = listOf(ChangeRolesNode.Inputs(listType = inputs.listType)),
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(modifier = modifier, navModel = navModel)
    }

    override val roomId: RoomId = inputs.joinedRoom.roomId

    override suspend fun waitForRoleChanged() {
        waitForChildAttached<ChangeRolesNode>().waitForRoleChanged()
    }
}
