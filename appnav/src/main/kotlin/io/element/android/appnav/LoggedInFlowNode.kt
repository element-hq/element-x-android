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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.Coil
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.loggedin.LoggedInNode
import io.element.android.appnav.room.RoomFlowNode
import io.element.android.appnav.room.RoomLoadedFlowNode
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.features.invitelist.api.InviteListEntryPoint
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.features.ftue.api.FtueEntryPoint
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.deeplink.DeeplinkData
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.ui.di.MatrixUIBindings
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
class LoggedInFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val roomListEntryPoint: RoomListEntryPoint,
    private val preferencesEntryPoint: PreferencesEntryPoint,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    private val verifySessionEntryPoint: VerifySessionEntryPoint,
    private val inviteListEntryPoint: InviteListEntryPoint,
    private val ftueEntryPoint: FtueEntryPoint,
    private val coroutineScope: CoroutineScope,
    private val networkMonitor: NetworkMonitor,
    private val notificationDrawerManager: NotificationDrawerManager,
    private val ftueState: FtueState,
    snackbarDispatcher: SnackbarDispatcher,
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
        fun onFlowCreated(identifier: String, client: MatrixClient) = Unit

        fun onFlowReleased(identifier: String, client: MatrixClient) = Unit
    }

    data class Inputs(
        val matrixClient: MatrixClient
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val syncService = inputs.matrixClient.syncService()
    private val loggedInFlowProcessor = LoggedInEventProcessor(
        snackbarDispatcher,
        inputs.matrixClient.roomMembershipObserver(),
        inputs.matrixClient.sessionVerificationService(),
    )

    override fun onBuilt() {
        super.onBuilt()

        lifecycle.subscribe(
            onCreate = {
                plugins<LifecycleCallback>().forEach { it.onFlowCreated(id, inputs.matrixClient) }
                val imageLoaderFactory = bindings<MatrixUIBindings>().loggedInImageLoaderFactory()
                Coil.setImageLoader(imageLoaderFactory)
                appNavigationStateService.onNavigateToSession(id, inputs.matrixClient.sessionId)
                // TODO We do not support Space yet, so directly navigate to main space
                appNavigationStateService.onNavigateToSpace(id, MAIN_SPACE)
                loggedInFlowProcessor.observeEvents(coroutineScope)

                if (ftueState.shouldDisplayFlow.value) {
                    backstack.push(NavTarget.Ftue)
                }
            },
            onResume = {
                lifecycleScope.launch {
                    syncService.startSync()
                }
            },
            onPause = {
                syncService.stopSync()
            },
            onDestroy = {
                plugins<LifecycleCallback>().forEach { it.onFlowReleased(id, inputs.matrixClient) }
                appNavigationStateService.onLeavingSpace(id)
                appNavigationStateService.onLeavingSession(id)
                loggedInFlowProcessor.stopObserving()
            }
        )

        observeSyncStateAndNetworkStatus()
    }

    private fun observeSyncStateAndNetworkStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                combine(
                    syncService.syncState,
                    networkMonitor.connectivity
                ) { syncState, networkStatus ->
                    syncState == SyncState.Error && networkStatus == NetworkStatus.Online
                }
                    .distinctUntilChanged()
                    .collect { restartSync ->
                        if (restartSync) {
                            syncService.startSync()
                        }
                    }
            }
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Permanent : NavTarget

        @Parcelize
        object RoomList : NavTarget

        @Parcelize
        data class Room(
            val roomId: RoomId,
            val initialElement: RoomLoadedFlowNode.NavTarget = RoomLoadedFlowNode.NavTarget.Messages
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
        object Ftue : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
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
                        backstack.push(NavTarget.Room(roomId, initialElement = RoomLoadedFlowNode.NavTarget.RoomDetails))
                    }

                    override fun onReportBugClicked() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }
                }
                roomListEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            is NavTarget.Room -> {
                val nodeLifecycleCallbacks = plugins<NodeLifecycleCallback>()
                val callback = object : RoomLoadedFlowNode.Callback {
                    override fun onForwardedToSingleRoom(roomId: RoomId) {
                        coroutineScope.launch { attachRoom(roomId) }
                    }
                }
                val inputs = RoomFlowNode.Inputs(roomId = navTarget.roomId, initialElement = navTarget.initialElement)
                createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs, callback) + nodeLifecycleCallbacks)
            }
            NavTarget.Settings -> {
                val callback = object : PreferencesEntryPoint.Callback {
                    override fun onOpenBugReport() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }

                    override fun onVerifyClicked() {
                        backstack.push(NavTarget.VerifySession)
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
            NavTarget.Ftue -> {
                ftueEntryPoint.nodeBuilder(this, buildContext)
                    .callback(object : FtueEntryPoint.Callback {
                        override fun onFtueFlowFinished() {
                            backstack.pop()
                        }
                    }).build()
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

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            Children(
                navModel = backstack,
                modifier = Modifier,
                // Animate navigation to settings and to a room
                transitionHandler = rememberDefaultTransitionHandler(),
            )

            val isFtueDisplayed by ftueState.shouldDisplayFlow.collectAsState()

            if (!isFtueDisplayed) {
                PermanentChild(navTarget = NavTarget.Permanent)
            }
        }
    }

    internal suspend fun attachRoom(deeplinkData: DeeplinkData.Room) {
        backstack.push(NavTarget.Room(deeplinkData.roomId))
    }

    internal suspend fun attachInviteList(deeplinkData: DeeplinkData.InviteList) {
        notificationDrawerManager.clearMembershipNotificationForSession(deeplinkData.sessionId)
        backstack.push(NavTarget.InviteList)
    }
}
