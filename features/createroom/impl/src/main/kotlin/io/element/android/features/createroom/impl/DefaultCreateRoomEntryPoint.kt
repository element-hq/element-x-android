/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesBinding(SessionScope::class)
class DefaultCreateRoomEntryPoint : CreateRoomEntryPoint {
    private var isSpace = false
    private var parentSpaceId: RoomId? = null

    override fun setIsSpace(isSpace: Boolean): CreateRoomEntryPoint {
        this.isSpace = isSpace
        return this
    }

    override fun setParentSpace(parentSpaceId: RoomId): CreateRoomEntryPoint {
        this.parentSpaceId = parentSpaceId
        return this
    }

    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: CreateRoomEntryPoint.Callback,
    ): Node {
        val inputs = CreateRoomFlowNode.Inputs(isSpace = isSpace, parentSpaceId = parentSpaceId)
        return parentNode.createNode<CreateRoomFlowNode>(buildContext, listOf(inputs, callback))
    }
}
