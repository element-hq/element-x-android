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

package io.element.android.features.roomdetails.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint.InitialTarget
import io.element.android.features.roomdetails.impl.RoomDetailsFlowNode.NavTarget
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRoomDetailsEntryPoint @Inject constructor() : RoomDetailsEntryPoint {
    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): RoomDetailsEntryPoint.NodeBuilder {
        return object : RoomDetailsEntryPoint.NodeBuilder {
            val plugins = ArrayList<Plugin>()

            override fun params(params: RoomDetailsEntryPoint.Params): RoomDetailsEntryPoint.NodeBuilder {
                plugins += params
                return this
            }

            override fun callback(callback: RoomDetailsEntryPoint.Callback): RoomDetailsEntryPoint.NodeBuilder {
                plugins += callback
                return this
            }

            override fun build(): Node {
                return parentNode.createNode<RoomDetailsFlowNode>(buildContext, plugins)
            }
        }
    }
}

internal fun InitialTarget.toNavTarget() = when (this) {
    is InitialTarget.RoomDetails -> NavTarget.RoomDetails
    is InitialTarget.RoomMemberDetails -> NavTarget.RoomMemberDetails(roomMemberId)
    is InitialTarget.RoomNotificationSettings -> NavTarget.RoomNotificationSettings(showUserDefinedSettingStyle = true)
}
