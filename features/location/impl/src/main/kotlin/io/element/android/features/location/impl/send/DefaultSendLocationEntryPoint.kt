/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.send

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.location.api.SendLocationEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.matrix.api.timeline.Timeline

@ContributesBinding(AppScope::class)
@Inject
class DefaultSendLocationEntryPoint : SendLocationEntryPoint {
    override fun builder(timelineMode: Timeline.Mode): SendLocationEntryPoint.Builder {
        return Builder(timelineMode)
    }

    class Builder(private val timelineMode: Timeline.Mode) : SendLocationEntryPoint.Builder {
        override fun build(parentNode: Node, buildContext: BuildContext): Node {
            return parentNode.createNode<SendLocationNode>(
                buildContext = buildContext,
                plugins = listOf(SendLocationNode.Inputs(timelineMode))
            )
        }
    }
}
