/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint

@ContributesBinding(AppScope::class)
class DefaultRoomSelectEntryPoint : RoomSelectEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: RoomSelectEntryPoint.Params,
        callback: RoomSelectEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<RoomSelectNode>(
            buildContext = buildContext,
            plugins = listOf(
                RoomSelectNode.Inputs(
                    sessionId = params.sessionId,
                    mode = params.mode,
                    maxNumberOfRooms = params.maxNumberOfRooms,
                ),
                callback,
            )
        )
    }
}
