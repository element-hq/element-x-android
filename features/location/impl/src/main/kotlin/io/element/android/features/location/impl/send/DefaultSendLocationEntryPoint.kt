/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.send

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.location.api.SendLocationEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.matrix.api.timeline.Timeline

@ContributesBinding(AppScope::class)
class DefaultSendLocationEntryPoint : SendLocationEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        timelineMode: Timeline.Mode,
    ): Node {
        return parentNode.createNode<SendLocationNode>(
            buildContext = buildContext,
            plugins = listOf(SendLocationNode.Inputs(timelineMode))
        )
    }
}
