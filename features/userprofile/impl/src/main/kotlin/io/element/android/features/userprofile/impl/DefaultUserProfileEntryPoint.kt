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

package io.element.android.features.userprofile.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.userprofile.api.UserProfileEntryPoint
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultUserProfileEntryPoint @Inject constructor() : UserProfileEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): UserProfileEntryPoint.NodeBuilder {
        return object : UserProfileEntryPoint.NodeBuilder {
            val plugins = ArrayList<Plugin>()

            override fun params(params: UserProfileEntryPoint.Params): UserProfileEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun callback(callback: UserProfileEntryPoint.Callback): UserProfileEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<UserProfileFlowNode>(buildContext, plugins)
            }
        }
    }
}
