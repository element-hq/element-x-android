/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint

@ContributesBinding(AppScope::class)
class DefaultPushHistoryEntryPoint : PushHistoryEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: PushHistoryEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<PushHistoryNode>(buildContext, listOf(callback))
    }
}
