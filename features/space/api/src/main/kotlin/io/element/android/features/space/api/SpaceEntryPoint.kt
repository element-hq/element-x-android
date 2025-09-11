/*
 * Copyright 2025 New Vector Ltd.
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

interface SpaceEntryPoint : FeatureEntryPoint {
    fun nodeBuilder(
        parentNode: Node,
        buildContext: BuildContext,
    ): NodeBuilder

    interface NodeBuilder {
        fun inputs(inputs: Inputs): NodeBuilder
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }

    data class Inputs(
        val roomId: RoomId
    ) : Plugin

    interface Callback : Plugin {
        fun onOpenRoom(roomId: RoomId)
    }
}
