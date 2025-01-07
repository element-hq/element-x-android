/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomaliasesolver.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias

interface RoomAliasResolverEntryPoint : FeatureEntryPoint {
    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface NodeBuilder {
        fun callback(callback: Callback): NodeBuilder
        fun params(params: Params): NodeBuilder
        fun build(): Node
    }

    interface Callback : Plugin {
        fun onAliasResolved(data: ResolvedRoomAlias)
    }

    data class Params(
        val roomAlias: RoomAlias
    ) : NodeInputs
}
