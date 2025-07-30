/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultChangeRoomMemberRolesEntyPoint @Inject constructor() : ChangeRoomMemberRolesEntryPoint {
    private lateinit var changeRoomMemberRolesListType: ChangeRoomMemberRolesListType
    private lateinit var room: JoinedRoom
    private var callback: ChangeRoomMemberRolesEntryPoint.Callback? = null

    override fun room(room: JoinedRoom): ChangeRoomMemberRolesEntryPoint {
        this.room = room
        return this
    }

    override fun listType(changeRoomMemberRolesListType: ChangeRoomMemberRolesListType): ChangeRoomMemberRolesEntryPoint {
        this.changeRoomMemberRolesListType = changeRoomMemberRolesListType
        return this
    }

    override fun callback(callback: ChangeRoomMemberRolesEntryPoint.Callback): ChangeRoomMemberRolesEntryPoint {
        this.callback = callback
        return this
    }

    override fun createNode(parentNode: Node, buildContext: BuildContext): Node {
        return parentNode.createNode<ChangeRoomMemberRolesRootNode>(
            buildContext = buildContext,
            plugins = listOfNotNull(
                ChangeRoomMemberRolesRootNode.Inputs(joinedRoom = room, listType = changeRoomMemberRolesListType),
                callback
            )
        )
    }
}
