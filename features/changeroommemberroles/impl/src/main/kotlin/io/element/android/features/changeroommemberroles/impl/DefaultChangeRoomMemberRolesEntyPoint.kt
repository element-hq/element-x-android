/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import dev.zacsweers.metro.Inject

@ContributesBinding(SessionScope::class)
@Inject
class DefaultChangeRoomMemberRolesEntyPoint() : ChangeRoomMemberRolesEntryPoint {
    override fun builder(parentNode: Node, buildContext: BuildContext): ChangeRoomMemberRolesEntryPoint.Builder {
        return object : ChangeRoomMemberRolesEntryPoint.Builder {
            private lateinit var changeRoomMemberRolesListType: ChangeRoomMemberRolesListType
            private lateinit var room: JoinedRoom

            override fun room(room: JoinedRoom): ChangeRoomMemberRolesEntryPoint.Builder {
                this.room = room
                return this
            }

            override fun listType(changeRoomMemberRolesListType: ChangeRoomMemberRolesListType): ChangeRoomMemberRolesEntryPoint.Builder {
                this.changeRoomMemberRolesListType = changeRoomMemberRolesListType
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<ChangeRoomMemberRolesRootNode>(
                    buildContext = buildContext,
                    plugins = listOf(
                        ChangeRoomMemberRolesRootNode.Inputs(joinedRoom = room, listType = changeRoomMemberRolesListType),
                    )
                )
            }
        }
    }
}
