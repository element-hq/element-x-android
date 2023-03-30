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

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.roomdetails.impl.members.RoomMemberListNode
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.RoomScope
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class RoomDetailsFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BackstackNode<RoomDetailsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.RoomDetails,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object RoomDetails : NavTarget

        @Parcelize
        object RoomMemberList : NavTarget
    }

    interface Callback : Plugin {
        fun openRoomMemberList()
    }

    val callback = object : Callback {
        override fun openRoomMemberList() {
            backstack.push(NavTarget.RoomMemberList)
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomDetails -> createNode<RoomDetailsNode>(buildContext, listOf(callback))
            NavTarget.RoomMemberList -> createNode<RoomMemberListNode>(buildContext)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
