/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import android.content.Context
import android.content.Intent
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.lockscreen.impl.unlock.activity.PinUnlockActivity
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
@Inject
class DefaultLockScreenEntryPoint() : LockScreenEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext, navTarget: LockScreenEntryPoint.Target): LockScreenEntryPoint.NodeBuilder {
        val callbacks = mutableListOf<LockScreenEntryPoint.Callback>()

        return object : LockScreenEntryPoint.NodeBuilder {
            override fun callback(callback: LockScreenEntryPoint.Callback): LockScreenEntryPoint.NodeBuilder {
                callbacks += callback
                return this
            }

            override fun build(): Node {
                val inputs = LockScreenFlowNode.Inputs(
                    when (navTarget) {
                        LockScreenEntryPoint.Target.Setup -> LockScreenFlowNode.NavTarget.Setup
                        LockScreenEntryPoint.Target.Settings -> LockScreenFlowNode.NavTarget.Settings
                    }
                )
                val plugins = listOf(inputs) + callbacks
                return parentNode.createNode<LockScreenFlowNode>(buildContext, plugins)
            }
        }
    }

    override fun pinUnlockIntent(context: Context): Intent {
        return PinUnlockActivity.newIntent(context)
    }
}
