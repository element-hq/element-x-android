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

package io.element.android.x.node

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.Coil
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.features.preferences.PreferencesFlowNode
import io.element.android.features.roomlist.RoomListNode
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.matrix.MatrixClient
import io.element.android.libraries.matrix.core.RoomId
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.libraries.matrix.ui.di.MatrixUIBindings
import io.element.android.x.di.SessionComponent
import kotlinx.parcelize.Parcelize

class LoggedInFlowNode(
    buildContext: BuildContext,
    val sessionId: SessionId,
    private val matrixClient: MatrixClient,
    private val onOpenBugReport: () -> Unit,
    private val backstack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.RoomList,
        savedStateMap = buildContext.savedStateMap,
    ),
) : ParentNode<LoggedInFlowNode.NavTarget>(
    navModel = backstack,
    buildContext = buildContext
), DaggerComponentOwner {

    override val daggerComponent: Any by lazy {
        parent!!.bindings<SessionComponent.ParentBindings>().sessionComponentBuilder().client(matrixClient).build()
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                val imageLoaderFactory = bindings<MatrixUIBindings>().loggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
                matrixClient.startSync()
            },
            onDestroy = {
                val imageLoaderFactory = bindings<MatrixUIBindings>().notLoggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
            }
        )
    }

    private val roomListCallback = object : RoomListNode.Callback {
        override fun onRoomClicked(roomId: RoomId) {
            backstack.push(NavTarget.Room(roomId))
        }

        override fun onSettingsClicked() {
            backstack.push(NavTarget.Settings)
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object RoomList : NavTarget

        @Parcelize
        data class Room(val roomId: RoomId) : NavTarget

        @Parcelize
        object Settings : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomList -> {
                createNode<RoomListNode>(buildContext, plugins = listOf(roomListCallback))
            }
            is NavTarget.Room -> {
                val room = matrixClient.getRoom(roomId = navTarget.roomId)
                if (room == null) {
                    // TODO CREATE UNKNOWN ROOM NODE
                    node(buildContext) {
                        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Unknown room with id = ${navTarget.roomId}")
                        }
                    }
                } else {
                    RoomFlowNode(buildContext, room)
                }
            }
            NavTarget.Settings -> {
                PreferencesFlowNode(buildContext, onOpenBugReport)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
        )
    }
}
