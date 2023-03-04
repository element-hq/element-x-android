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

package io.element.android.x

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.matrix.MatrixClient
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.x.di.MainDaggerComponentOwner
import io.element.android.x.di.RoomComponent
import io.element.android.x.di.SessionComponent
import io.element.android.x.node.LoggedInFlowNode
import io.element.android.x.node.RoomFlowNode
import io.element.android.x.node.RootFlowNode

class MainNode(
    buildContext: BuildContext,
    private val mainDaggerComponentOwner: MainDaggerComponentOwner,
) :
    ParentNode<MainNode.NavTarget>(
        navModel = PermanentNavModel(
            navTargets = setOf(NavTarget),
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
    ),
    DaggerComponentOwner by mainDaggerComponentOwner {

    private val loggedInFlowNodeCallback = object : LoggedInFlowNode.LifecycleCallback {
        override fun onFlowCreated(client: MatrixClient) {
            val component = bindings<SessionComponent.ParentBindings>().sessionComponentBuilder().client(client).build()
            mainDaggerComponentOwner.addComponent(client.sessionId.value, component)
        }

        override fun onFlowReleased(client: MatrixClient) {
            mainDaggerComponentOwner.removeComponent(client.sessionId.value)
        }
    }

    private val roomFlowNodeCallback = object : RoomFlowNode.LifecycleCallback {
        override fun onFlowCreated(room: MatrixRoom) {
            val component = bindings<RoomComponent.ParentBindings>().roomComponentBuilder().room(room).build()
            mainDaggerComponentOwner.addComponent(room.roomId.value, component)
        }

        override fun onFlowReleased(room: MatrixRoom) {
            mainDaggerComponentOwner.removeComponent(room.roomId.value)
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return createNode<RootFlowNode>(buildContext, plugins = listOf(loggedInFlowNodeCallback, roomFlowNodeCallback))
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = navModel)
    }

    object NavTarget
}
