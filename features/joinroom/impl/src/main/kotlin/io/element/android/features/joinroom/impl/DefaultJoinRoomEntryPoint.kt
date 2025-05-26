/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultJoinRoomEntryPoint @Inject constructor() : JoinRoomEntryPoint {
    override fun createNode(parentNode: Node, buildContext: BuildContext, inputs: JoinRoomEntryPoint.Inputs): Node {
        return parentNode.createNode<JoinRoomFlowNode>(
            buildContext = buildContext,
            plugins = listOf(inputs)
        )
    }
}
