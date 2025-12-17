/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint.InitialTarget
import io.element.android.features.roomdetails.impl.RoomDetailsFlowNode.NavTarget
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultRoomDetailsEntryPoint : RoomDetailsEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: RoomDetailsEntryPoint.Params,
        callback: RoomDetailsEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<RoomDetailsFlowNode>(
            buildContext = buildContext,
            plugins = listOf(params, callback)
        )
    }
}

internal fun InitialTarget.toNavTarget() = when (this) {
    is InitialTarget.RoomDetails -> NavTarget.RoomDetails
    is InitialTarget.RoomMemberDetails -> NavTarget.RoomMemberDetails(roomMemberId)
    is InitialTarget.RoomNotificationSettings -> NavTarget.RoomNotificationSettings(showUserDefinedSettingStyle = true)
    InitialTarget.RoomMemberList -> NavTarget.RoomMemberList
}
