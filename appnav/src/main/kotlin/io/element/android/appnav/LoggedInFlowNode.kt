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
import com.bumble.appyx.core.composable.PermanentChild
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
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
import io.element.android.features.ftue.api.FtueEntryPoint
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.invitelist.api.InviteListEntryPoint
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.lockscreen.api.LockScreenLockState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.verifysession.api.VerifySessionEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.waitForChildAttached
import io.element.android.libraries.deeplink.DeeplinkData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
class LoggedInFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val roomListEntryPoint: RoomListEntryPoint,
    private val preferencesEntryPoint: PreferencesEntryPoint,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    private val verifySessionEntryPoint: VerifySessionEntryPoint,
    private val secureBackupEntryPoint: SecureBackupEntryPoint,
    private val inviteListEntryPoint: InviteListEntryPoint,
    private val ftueEntryPoint: FtueEntryPoint,
    private val coroutineScope: CoroutineScope,
    private val networkMonitor: NetworkMonitor,
    private val notificationDrawerManager: NotificationDrawerManager,
    private val ftueState: FtueState,
    private val lockScreenEntryPoint: LockScreenEntryPoint,
    private val lockScreenStateService: LockScreenService,
    private val matrixClient: MatrixClient,
    private val sessionVerificationService: SessionVerificationService,
    snackbarDispatcher: SnackbarDispatcher,
) : BaseFlowNode<LoggedInFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Placeholder,
        savedStateMap = buildContext.savedStateMap,
    ),
    permanentNavModel = PermanentNavModel(
        navTargets = setOf(NavTarget.LoggedInPermanent, NavTarget.LockPermanent),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun onOpenBugReport()
    }

    private val syncService = matrixClient.syncService()
    private val loggedInFlowProcessor = LoggedInEventProcessor(
        snackbarDispatcher,
        matrixClient.roomMembershipObserver(),
    )

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                appNavigationStateService.onNavigateToSession(id, matrixClient.sessionId)
                // TODO We do not support Space yet, so directly navigate to main space
                appNavigationStateService.onNavigateToSpace(id, MAIN_SPACE)
                loggedInFlowProcessor.observeEvents(coroutineScope)

                ftueState.shouldDisplayFlow
                    .filter { it }
                    .onEach {
                        backstack.push(NavTarget.Ftue)
                    }
                    .launchIn(lifecycleScope)

                // Attach the root node as soon as we know the first session verification status and the FTUE shouldn't be displayed.
                // This prevents the room list from being displayed while the session is not verified.
                combine(
                    sessionVerificationService.sessionVerifiedStatus,
                    ftueState.shouldDisplayFlow,
                ) { sessionVerifiedStatus, shouldDisplayFtue ->
                        sessionVerifiedStatus to shouldDisplayFtue
                    }
                    .filter { (sessionVerifiedStatus, shouldDisplayFtue) ->
                        sessionVerifiedStatus.isVerified() && !shouldDisplayFtue
                    }
                    .onEach { attachRoot() }
                    .launchIn(lifecycleScope)
            },
            onStop = {
                coroutineScope.launch {
                    // Counterpart startSync is done in observeSyncStateAndNetworkStatus method.
                    syncService.stopSync()
                }
            },
            onDestroy = {
                appNavigationStateService.onLeavingSpace(id)
                appNavigationStateService.onLeavingSession(id)
                loggedInFlowProcessor.stopObserving()
            }
        )
        observeSyncStateAndNetworkStatus()
        observeInvitesLoadingState()
    }

    private fun observeInvitesLoadingState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                matrixClient.roomListService.invites.loadingState
                    .collect { inviteState ->
                        when (inviteState) {
                            is RoomList.LoadingState.Loaded -> if (inviteState.numberOfRooms == 0) {
                                backstack.removeLast(NavTarget.InviteList)
                            }
                            RoomList.LoadingState.NotLoaded -> Unit
                        }
                    }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSyncStateAndNetworkStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    // small debounce to avoid spamming startSync when the state is changing quickly in case of error.
                    syncService.syncState.debounce(100),
                    networkMonitor.connectivity
                ) { syncState, networkStatus ->
                    Pair(syncState, networkStatus)
                }
                    .collect { (syncState, networkStatus) ->
                        Timber.d("Sync state: $syncState, network status: $networkStatus")
                        if (syncState != SyncState.Running && networkStatus == NetworkStatus.Online) {
                            syncService.startSync()
                        }
                    }
            }
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Placeholder: NavTarget

        @Parcelize
        data object LoggedInPermanent : NavTarget

        @Parcelize
        data object LockPermanent : NavTarget

        @Parcelize
        data object RoomList : NavTarget

        @Parcelize
        data class Room(
            val roomId: RoomId,
            val initialElement: RoomLoadedFlowNode.NavTarget = RoomLoadedFlowNode.NavTarget.Messages
        ) : NavTarget

        @Parcelize
        data class Settings(
            val initialElement: PreferencesEntryPoint.InitialTarget = PreferencesEntryPoint.InitialTarget.Root
        ) : NavTarget

        @Parcelize
        data object CreateRoom : NavTarget

        @Parcelize
        data object VerifySession : NavTarget

        @Parcelize
        data class SecureBackup(
            val initialElement: SecureBackupEntryPoint.InitialTarget = SecureBackupEntryPoint.InitialTarget.Root
        ) : NavTarget

        @Parcelize
        data object InviteList : NavTarget

        @Parcelize
        data object Ftue : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Placeholder -> createNode<PlaceholderNode>(buildContext)
            NavTarget.LoggedInPermanent -> {
                createNode<LoggedInNode>(buildContext)
            }
            NavTarget.LockPermanent -> {
                lockScreenEntryPoint.nodeBuilder(this, buildContext)
                    .target(LockScreenEntryPoint.Target.Unlock)
                    .build()
            }
            NavTarget.RoomList -> {
                val callback = object : RoomListEntryPoint.Callback {
                    override fun onRoomClicked(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId))
                    }

                    override fun onSettingsClicked() {
                        backstack.push(NavTarget.Settings())
                    }

                    override fun onCreateRoomClicked() {
                        backstack.push(NavTarget.CreateRoom)
                    }

                    override fun onSessionVerificationClicked() {
                        backstack.push(NavTarget.VerifySession)
                    }

                    override fun onSessionConfirmRecoveryKeyClicked() {
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey))
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
                val callback = object : RoomLoadedFlowNode.Callback {
                    override fun onOpenRoom(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId))
                    }

                    override fun onForwardedToSingleRoom(roomId: RoomId) {
                        coroutineScope.launch { attachRoom(roomId) }
                    }

                    override fun onOpenGlobalNotificationSettings() {
                        backstack.push(NavTarget.Settings(PreferencesEntryPoint.InitialTarget.NotificationSettings))
                    }
                }
                val inputs = RoomFlowNode.Inputs(roomId = navTarget.roomId, initialElement = navTarget.initialElement)
                createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.Settings -> {
                val callback = object : PreferencesEntryPoint.Callback {
                    override fun onOpenBugReport() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }

                    override fun onVerifyClicked() {
                        backstack.push(NavTarget.VerifySession)
                    }

                    override fun onSecureBackupClicked() {
                        backstack.push(NavTarget.SecureBackup())
                    }

                    override fun onOpenRoomNotificationSettings(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId, initialElement = RoomLoadedFlowNode.NavTarget.RoomNotificationSettings))
                    }
                }
                val inputs = PreferencesEntryPoint.Params(navTarget.initialElement)
                return preferencesEntryPoint.nodeBuilder(this, buildContext)
                    .params(inputs)
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
                val callback = object : VerifySessionEntryPoint.Callback {
                    override fun onEnterRecoveryKey() {
                        backstack.replace(
                            NavTarget.SecureBackup(
                                initialElement = SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey
                            )
                        )
                    }

                    override fun onDone() {
                        backstack.pop()
                    }
                }
                verifySessionEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            is NavTarget.SecureBackup -> {
                secureBackupEntryPoint.nodeBuilder(this, buildContext)
                    .params(SecureBackupEntryPoint.Params(initialElement = navTarget.initialElement))
                    .build()
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
                            lifecycleScope.launch { attachRoot() }
                        }
                    })
                    .build()
            }
        }
    }

    suspend fun attachRoot() {
        if (!canShowRoot(ftueState, sessionVerificationService.sessionVerifiedStatus.value)) return
        attachChild<Node> {
            backstack.singleTop(NavTarget.RoomList)
        }
    }

    suspend fun attachRoom(roomId: RoomId) {
        if (!canShowRoot(ftueState, sessionVerificationService.sessionVerifiedStatus.value)) return
        attachChild<RoomFlowNode> {
            backstack.singleTop(NavTarget.RoomList)
            backstack.push(NavTarget.Room(roomId))
        }
    }

    internal suspend fun attachInviteList(deeplinkData: DeeplinkData.InviteList) = withContext(lifecycleScope.coroutineContext) {
        if (!canShowRoot(ftueState, sessionVerificationService.sessionVerifiedStatus.value)) return@withContext
        notificationDrawerManager.clearMembershipNotificationForSession(deeplinkData.sessionId)
        backstack.singleTop(NavTarget.RoomList)
        backstack.push(NavTarget.InviteList)
        waitForChildAttached<Node, NavTarget> { navTarget ->
            navTarget is NavTarget.InviteList
        }
        Unit
    }

    private fun canShowRoot(ftueState: FtueState, sessionVerifiedStatus: SessionVerifiedStatus): Boolean {
        return !ftueState.shouldDisplayFlow.value && sessionVerifiedStatus.isVerified()
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            val lockScreenState by lockScreenStateService.lockState.collectAsState()
            val isFtueDisplayed by ftueState.shouldDisplayFlow.collectAsState()
            BackstackView()
            if (!isFtueDisplayed) {
                PermanentChild(permanentNavModel = permanentNavModel, navTarget = NavTarget.LoggedInPermanent)
            }
            if (lockScreenState == LockScreenLockState.Locked) {
                PermanentChild(permanentNavModel = permanentNavModel, navTarget = NavTarget.LockPermanent)
            }
        }
    }

    @ContributesNode(AppScope::class)
    class PlaceholderNode @AssistedInject constructor(
        @Assisted buildContext: BuildContext,
        @Assisted plugins: List<Plugin>,
    ) : Node(buildContext, plugins = plugins)
}
