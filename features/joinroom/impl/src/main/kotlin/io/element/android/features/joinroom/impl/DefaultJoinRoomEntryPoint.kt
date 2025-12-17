/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultJoinRoomEntryPoint : JoinRoomEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: JoinRoomEntryPoint.Inputs,
    ): Node {
        return parentNode.createNode<JoinRoomFlowNode>(
            buildContext = buildContext,
            plugins = listOf(inputs)
        )
    }
}
