/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroes.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom

interface ChangeRoomMemberRolesEntryPoint : FeatureEntryPoint {
    fun builder(parentNode: Node, buildContext: BuildContext): Builder

    interface Builder {
        fun room(room: JoinedRoom): Builder
        fun listType(changeRoomMemberRolesListType: ChangeRoomMemberRolesListType): Builder
        fun build(): Node
    }

    interface NodeProxy {
        val roomId: RoomId
        suspend fun waitForRoleChanged()
    }
}

enum class ChangeRoomMemberRolesListType : NodeInputs {
    SelectNewOwnersWhenLeaving,
    Admins,
    Moderators
}
