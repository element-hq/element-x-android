/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.spaces.SpaceRoom

interface SpaceEntryPoint : FeatureEntryPoint {
    fun nodeBuilder(
        parentNode: Node,
        buildContext: BuildContext,
    ): NodeBuilder

    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }

    sealed interface Params : Plugin {
        data class Id(val roomId: RoomId) : Params
        data class Full(val spaceRoom: SpaceRoom) : Params

        fun roomId(): RoomId {
            return when (this) {
                is Id -> roomId
                is Full -> spaceRoom.roomId
            }
        }
    }

    interface Callback : Plugin {
        fun onOpenRoom(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>)
    }
}
