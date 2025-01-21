/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

import android.content.Intent
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.RoomId

interface ShareEntryPoint : FeatureEntryPoint {
    data class Params(val intent: Intent) : NodeInputs

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface Callback : Plugin {
        fun onDone(roomIds: List<RoomId>)
    }

    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }
}
