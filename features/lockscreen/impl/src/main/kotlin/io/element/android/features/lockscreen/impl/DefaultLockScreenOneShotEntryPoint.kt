/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.lockscreen.api.LockScreenOneShotEntryPoint
import io.element.android.features.lockscreen.impl.unlock.PinUnlockNode
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultLockScreenOneShotEntryPoint : LockScreenOneShotEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: LockScreenOneShotEntryPoint.Callback,
    ): Node {
        val internalCallback = object : PinUnlockNode.Callback {
            override fun onUnlock() = callback.onUnlocked()
            override fun onCancel() = callback.onCancel()
        }
        val inputs = PinUnlockNode.Inputs(
            forFeatureUnlock = true,
        )
        return parentNode.createNode<PinUnlockNode>(
            buildContext = buildContext,
            plugins = listOf(
                internalCallback,
                inputs,
            )
        )
    }
}
