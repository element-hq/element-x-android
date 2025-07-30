/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroes.api

import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.SimpleFeatureEntryPoint
import io.element.android.libraries.matrix.api.room.JoinedRoom

interface ChangeRoomMemberRolesEntryPoint : SimpleFeatureEntryPoint {
    fun room(room: JoinedRoom): ChangeRoomMemberRolesEntryPoint
    fun listType(changeRoomMemberRolesListType: ChangeRoomMemberRolesListType): ChangeRoomMemberRolesEntryPoint
    fun callback(callback: Callback): ChangeRoomMemberRolesEntryPoint

    interface Callback : Plugin {
        fun onRolesChanged()
    }
}

enum class ChangeRoomMemberRolesListType : NodeInputs{
    SelectNewOwnersWhenLeaving,
    Admins,
    Moderators
}
