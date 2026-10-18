/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom

fun interface ChangeRoomMemberRolesEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        room: JoinedRoom,
        listType: ChangeRoomMemberRolesListType,
    ): Node

    /**
     * Lets a caller outside this feature wait for the role change to finish, which is needed when leaving a room depends on it.
     */
    interface NodeProxy {
        /** The room whose member roles are being changed. */
        val roomId: RoomId

        /**
         * Suspends until the user finishes or abandons the flow.
         *
         * @return true when roles were actually changed, false when the user backed out.
         */
        suspend fun waitForCompletion(): Boolean
    }
}

enum class ChangeRoomMemberRolesListType {
    SelectNewOwnersWhenLeaving,
    Admins,
    Moderators
}
