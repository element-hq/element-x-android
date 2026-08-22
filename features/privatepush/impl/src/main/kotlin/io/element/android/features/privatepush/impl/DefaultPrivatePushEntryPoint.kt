/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.privatepush.api.PrivatePushEntryPoint
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultPrivatePushEntryPoint : PrivatePushEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: PrivatePushEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<PrivatePushFlowNode>(buildContext, plugins = listOf(callback))
    }
}
