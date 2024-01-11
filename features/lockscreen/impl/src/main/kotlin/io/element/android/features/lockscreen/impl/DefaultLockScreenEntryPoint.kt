/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.lockscreen.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultLockScreenEntryPoint @Inject constructor() : LockScreenEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): LockScreenEntryPoint.NodeBuilder {
        var innerTarget: LockScreenEntryPoint.Target = LockScreenEntryPoint.Target.Unlock
        val callbacks = mutableListOf<LockScreenEntryPoint.Callback>()

        return object : LockScreenEntryPoint.NodeBuilder {
            override fun callback(callback: LockScreenEntryPoint.Callback): LockScreenEntryPoint.NodeBuilder {
                callbacks += callback
                return this
            }

            override fun target(target: LockScreenEntryPoint.Target): LockScreenEntryPoint.NodeBuilder {
                innerTarget = target
                return this
            }

            override fun build(): Node {
                val inputs = LockScreenFlowNode.Inputs(
                    when (innerTarget) {
                        LockScreenEntryPoint.Target.Unlock -> LockScreenFlowNode.NavTarget.Unlock
                        LockScreenEntryPoint.Target.Setup -> LockScreenFlowNode.NavTarget.Setup
                        LockScreenEntryPoint.Target.Settings -> LockScreenFlowNode.NavTarget.Settings
                    }
                )
                val plugins = listOf(inputs) + callbacks
                return parentNode.createNode<LockScreenFlowNode>(buildContext, plugins)
            }
        }
    }
}
