/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomaliasresolver.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.roomaliasesolver.api.RoomAliasResolverEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRoomAliasResolverEntryPoint @Inject constructor() : RoomAliasResolverEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): RoomAliasResolverEntryPoint.NodeBuilder {
        val plugins = ArrayList<Plugin>()

        return object : RoomAliasResolverEntryPoint.NodeBuilder {
            override fun callback(callback: RoomAliasResolverEntryPoint.Callback): RoomAliasResolverEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun params(params: RoomAliasResolverEntryPoint.Params): RoomAliasResolverEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<RoomAliasResolverNode>(buildContext, plugins)
            }
        }
    }
}
