/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.roles

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.rolesandpermissions.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.JoinedRoom

@ContributesBinding(SessionScope::class)
class DefaultChangeRoomMemberRolesEntyPoint : ChangeRoomMemberRolesEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        room: JoinedRoom,
        listType: ChangeRoomMemberRolesListType,
    ): Node {
        return parentNode.createNode<ChangeRoomMemberRolesRootNode>(
            buildContext = buildContext,
            plugins = listOf(
                ChangeRoomMemberRolesRootNode.Inputs(joinedRoom = room, listType = listType),
            )
        )
    }
}
