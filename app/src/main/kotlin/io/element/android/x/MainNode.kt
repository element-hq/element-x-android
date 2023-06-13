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

import android.content.Intent
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.appnav.LoggedInFlowNode
import io.element.android.appnav.RoomFlowNode
import io.element.android.appnav.RootFlowNode
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.x.di.MainDaggerComponentsOwner
import io.element.android.x.di.RoomComponent
import io.element.android.x.di.SessionComponent
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class MainNode(
    buildContext: BuildContext,
    private val mainDaggerComponentOwner: MainDaggerComponentsOwner,
    plugins: List<Plugin>,
) :
    ParentNode<MainNode.RootNavTarget>(
        navModel = PermanentNavModel(
            navTargets = setOf(RootNavTarget),
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
        plugins = plugins,
    ),
    DaggerComponentOwner by mainDaggerComponentOwner {

    private val loggedInFlowNodeCallback = object : LoggedInFlowNode.LifecycleCallback {
        override fun onFlowCreated(identifier: String, client: MatrixClient) {
            val component = bindings<SessionComponent.ParentBindings>().sessionComponentBuilder().client(client).build()
            mainDaggerComponentOwner.addComponent(identifier, component)
        }

        override fun onFlowReleased(identifier: String, client: MatrixClient) {
            mainDaggerComponentOwner.removeComponent(identifier)
        }
    }

    private val roomFlowNodeCallback = object : RoomFlowNode.LifecycleCallback {
        override fun onFlowCreated(identifier: String, room: MatrixRoom) {
            val component = bindings<RoomComponent.ParentBindings>().roomComponentBuilder().room(room).build()
            mainDaggerComponentOwner.addComponent(identifier, component)
        }

        override fun onFlowReleased(identifier: String, room: MatrixRoom) {
            mainDaggerComponentOwner.removeComponent(identifier)
        }
    }

    override fun resolve(navTarget: RootNavTarget, buildContext: BuildContext): Node {
        return createNode<RootFlowNode>(
            context = buildContext,
            plugins = listOf(
                loggedInFlowNodeCallback,
                roomFlowNodeCallback,
            )
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(navModel = navModel)
    }

    fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            waitForChildAttached<RootFlowNode>().handleIntent(intent)
        }
    }

    @Parcelize
    object RootNavTarget : Parcelable
}
