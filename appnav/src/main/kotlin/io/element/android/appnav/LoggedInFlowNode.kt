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
import androidx.lifecycle.lifecycleScope
import coil.Coil
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.ViewRoom
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.loggedin.LoggedInNode
import io.element.android.features.analytics.api.AnalyticsEntryPoint
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.features.invitelist.api.InviteListEntryPoint
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.di.MatrixUIBindings
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class LoggedInFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val roomListEntryPoint: RoomListEntryPoint,
    private val preferencesEntryPoint: PreferencesEntryPoint,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
    private val analyticsOptInEntryPoint: AnalyticsEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    private val verifySessionEntryPoint: VerifySessionEntryPoint,
    private val inviteListEntryPoint: InviteListEntryPoint,
    private val analyticsService: AnalyticsService,
    private val coroutineScope: CoroutineScope,
    snackbarDispatcher: SnackbarDispatcher,
) : BackstackNode<LoggedInFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.SplashScreen,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {

    private fun observeAnalyticsState() {
        analyticsService.didAskUserConsent()
            .distinctUntilChanged()
            .onEach { isConsentAsked ->
                if (isConsentAsked) {
                    switchToRoomList()
                } else {
                    switchToAnalytics()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun switchToRoomList() {
        backstack.safeRoot(NavTarget.RoomList)
    }

    private fun switchToAnalytics() {
        backstack.safeRoot(NavTarget.AnalyticsSettings)
    }

    interface Callback : Plugin {
        fun onOpenBugReport() = Unit
    }

    interface LifecycleCallback : NodeLifecycleCallback {
        fun onFlowCreated(identifier: String, client: MatrixClient) = Unit

        fun onFlowReleased(identifier: String, client: MatrixClient) = Unit
    }

    data class Inputs(
        val matrixClient: MatrixClient
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val loggedInFlowProcessor = LoggedInEventProcessor(
        snackbarDispatcher,
        inputs.matrixClient.roomMembershipObserver(),
        inputs.matrixClient.sessionVerificationService(),
    )

    override fun onBuilt() {
        super.onBuilt()
        observeAnalyticsState()
        lifecycle.subscribe(
            onCreate = {
                plugins<LifecycleCallback>().forEach { it.onFlowCreated(id, inputs.matrixClient) }
                val imageLoaderFactory = bindings<MatrixUIBindings>().loggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
                inputs.matrixClient.startSync()
                appNavigationStateService.onNavigateToSession(id, inputs.matrixClient.sessionId)
                // TODO We do not support Space yet, so directly navigate to main space
                appNavigationStateService.onNavigateToSpace(id, MAIN_SPACE)
                loggedInFlowProcessor.observeEvents(coroutineScope)
            },
            onDestroy = {
                val imageLoaderFactory = bindings<MatrixUIBindings>().notLoggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
                plugins<LifecycleCallback>().forEach { it.onFlowReleased(id, inputs.matrixClient) }
                appNavigationStateService.onLeavingSpace(id)
                appNavigationStateService.onLeavingSession(id)
                loggedInFlowProcessor.stopObserving()
            }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object SplashScreen : NavTarget

        @Parcelize
        object Permanent : NavTarget

        @Parcelize
        object RoomList : NavTarget

        @Parcelize
        data class Room(
            val roomId: RoomId,
            val initialElement: RoomFlowNode.NavTarget = RoomFlowNode.NavTarget.Messages
        ) : NavTarget

        @Parcelize
        object Settings : NavTarget

        @Parcelize
        object CreateRoom : NavTarget

        @Parcelize
        object VerifySession : NavTarget

        @Parcelize
        object InviteList : NavTarget

        @Parcelize
        object AnalyticsSettings : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.SplashScreen -> splashNode(buildContext)
            NavTarget.Permanent -> {
                createNode<LoggedInNode>(buildContext)
            }
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

                    override fun onInvitesClicked() {
                        backstack.push(NavTarget.InviteList)
                    }

                    override fun onRoomSettingsClicked(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId, initialElement = RoomFlowNode.NavTarget.RoomDetails))
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
                    analyticsService.capture(ViewRoom(isDM = room.isDirect))
                    val nodeLifecycleCallbacks = plugins<NodeLifecycleCallback>()
                    val callback = object : RoomFlowNode.Callback {
                        override fun onForwardedToSingleRoom(roomId: RoomId) {
                            coroutineScope.launch { attachRoom(roomId) }
                        }
                    }
                    val inputs = RoomFlowNode.Inputs(room, initialElement = navTarget.initialElement)
                    createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs, callback) + nodeLifecycleCallbacks)
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
                val callback = object : CreateRoomEntryPoint.Callback {
                    override fun onSuccess(roomId: RoomId) {
                        backstack.replace(NavTarget.Room(roomId))
                    }
                }

                createRoomEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            NavTarget.VerifySession -> {
                verifySessionEntryPoint.createNode(this, buildContext)
            }
            NavTarget.InviteList -> {
                val callback = object : InviteListEntryPoint.Callback {
                    override fun onBackClicked() {
                        backstack.pop()
                    }

                    override fun onInviteAccepted(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId))
                    }
                }

                inviteListEntryPoint.nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            NavTarget.AnalyticsSettings -> {
                analyticsOptInEntryPoint.createNode(this, buildContext)
            }
        }
    }

    suspend fun attachRoot(): Node {
        return attachChild {
            backstack.singleTop(NavTarget.RoomList)
        }
    }

    suspend fun attachRoom(roomId: RoomId): RoomFlowNode {
        return attachChild {
            backstack.singleTop(NavTarget.RoomList)
            backstack.push(NavTarget.Room(roomId))
        }
    }

    private fun splashNode(buildContext: BuildContext) = node(buildContext) {
        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            Children(
                navModel = backstack,
                modifier = Modifier,
                // Animate navigation to settings and to a room
                transitionHandler = rememberDefaultTransitionHandler(),
            )

            PermanentChild(navTarget = NavTarget.Permanent)
        }
    }
}
