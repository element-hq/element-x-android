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

package io.element.android.appnav

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.Coil
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.di.MatrixUIBindings
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class LoggedInFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val roomListEntryPoint: RoomListEntryPoint,
    private val preferencesEntryPoint: PreferencesEntryPoint,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
    private val verifySessionEntryPoint: VerifySessionEntryPoint,
) : BackstackNode<LoggedInFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.RoomList,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {

    interface Callback : Plugin {
        fun onOpenBugReport() = Unit
    }

    interface LifecycleCallback : NodeLifecycleCallback {
        fun onFlowCreated(client: MatrixClient) = Unit

        fun onFlowReleased(client: MatrixClient) = Unit
    }

    data class Inputs(
        val matrixClient: MatrixClient
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                plugins<LifecycleCallback>().forEach { it.onFlowCreated(inputs.matrixClient) }
                val imageLoaderFactory = bindings<MatrixUIBindings>().loggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
                inputs.matrixClient.startSync()
            },
            onDestroy = {
                val imageLoaderFactory = bindings<MatrixUIBindings>().notLoggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
                plugins<LifecycleCallback>().forEach { it.onFlowReleased(inputs.matrixClient) }
            }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object RoomList : NavTarget

        @Parcelize
        data class Room(val roomId: RoomId) : NavTarget

        @Parcelize
        object Settings : NavTarget

        @Parcelize
        object CreateRoom : NavTarget

        @Parcelize
        object VerifySession : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomList -> {
                val callback = object : RoomListEntryPoint.Callback {
                    override fun onRoomClicked(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId))
                    }

                    override fun onSettingsClicked() {
                        backstack.push(NavTarget.Settings)
                    }

                    override fun onCreateRoomClicked() {
                        backstack.push(NavTarget.CreateRoom)
                    }

                    override fun onSessionVerificationClicked() {
                        backstack.push(NavTarget.VerifySession)
                    }
                }
                roomListEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            is NavTarget.Room -> {
                val room = inputs.matrixClient.getRoom(roomId = navTarget.roomId)
                if (room == null) {
                    // TODO CREATE UNKNOWN ROOM NODE
                    node(buildContext) {
                        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Unknown room with id = ${navTarget.roomId}")
                        }
                    }
                } else {
                    val nodeLifecycleCallbacks = plugins<NodeLifecycleCallback>()
                    val inputs = RoomFlowNode.Inputs(room)
                    createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs) + nodeLifecycleCallbacks)
                }
            }
            NavTarget.Settings -> {
                val callback = object : PreferencesEntryPoint.Callback {
                    override fun onOpenBugReport() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }
                }
                preferencesEntryPoint.nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            NavTarget.CreateRoom -> {
                createRoomEntryPoint.createNode(this, buildContext)
            }
            NavTarget.VerifySession -> {
                verifySessionEntryPoint.createNode(this, buildContext)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backstack,
            modifier = modifier,
            // Animate navigation to settings and to a room
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}
