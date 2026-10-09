/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.ftue.api.SessionVerificationEntryPoint
import io.element.android.features.ftue.impl.sessionverification.FtueSessionVerificationFlowNode
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultSessionVerificationEntryPoint : SessionVerificationEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: SessionVerificationEntryPoint.Callback,
    ): Node {
        val flowNodeCallback = object : FtueSessionVerificationFlowNode.Callback {
            override fun onDone() {
                callback.onDone()
            }
        }
        return parentNode.createNode<FtueSessionVerificationFlowNode>(buildContext, listOf(flowNodeCallback))
    }
}
