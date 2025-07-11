/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.libraries.architecture.createNode
import javax.inject.Inject

class DefaultCreateRoomEntryPoint @Inject constructor(): CreateRoomEntryPoint {
    override fun createNode(parentNode: Node, buildContext: BuildContext): Node {
        return parentNode.createNode<CreateRoomFlowNode>(buildContext)
    }
}
