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

package io.element.android.features.invite.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invite.api.InviteListEntryPoint
import io.element.android.features.invite.impl.invitelist.InviteListNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultInviteListEntryPoint @Inject constructor() : InviteListEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): InviteListEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : InviteListEntryPoint.NodeBuilder {
            override fun callback(callback: InviteListEntryPoint.Callback): InviteListEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<InviteListNode>(buildContext, plugins)
            }
        }
    }
}
