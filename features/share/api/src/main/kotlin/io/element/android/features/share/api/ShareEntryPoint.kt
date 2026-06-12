/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId

interface ShareEntryPoint : FeatureEntryPoint {
    data class Params(val shareIntentData: ShareIntentData)

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        fun onDone(roomIds: List<RoomId>)
    }


    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }

    companion object {
        const val EXTRA_SHARE_TARGET_ROOM_ID = "io.element.android.features.share.extra.TARGET_ROOM_ID"
    }
}
