/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultSpaceEntryPoint : SpaceEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: SpaceEntryPoint.Inputs,
        callback: SpaceEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<SpaceFlowNode>(
            buildContext = buildContext,
            plugins = listOf(inputs, callback),
        )
    }
}
