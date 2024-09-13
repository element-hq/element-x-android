/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav

import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.loggedin.LoggedInNode
import io.element.android.appnav.loggedin.SendQueues
import io.element.android.appnav.room.RoomFlowNode
import io.element.android.appnav.room.RoomNavigationTarget
import io.element.android.appnav.room.joined.JoinedRoomLoadedFlowNode
import io.element.android.features.createroom.api.CreateRoomEntryPoint
import io.element.android.features.ftue.api.FtueEntryPoint
import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.logout.api.LogoutEntryPoint
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.features.roomdirectory.api.RoomDirectoryEntryPoint
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.features.userprofile.api.UserProfileEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.waitForNavTargetAttached
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.preferences.api.store.EnableNativeSlidingSyncUseCase
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Optional
import java.util.UUID

@ContributesNode(SessionScope::class)
class LoggedInFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val roomListEntryPoint: RoomListEntryPoint,
    private val preferencesEntryPoint: PreferencesEntryPoint,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    private val secureBackupEntryPoint: SecureBackupEntryPoint,
    private val userProfileEntryPoint: UserProfileEntryPoint,
    private val ftueEntryPoint: FtueEntryPoint,
    private val coroutineScope: CoroutineScope,
    private val networkMonitor: NetworkMonitor,
    private val ftueService: FtueService,
    private val roomDirectoryEntryPoint: RoomDirectoryEntryPoint,
    private val shareEntryPoint: ShareEntryPoint,
    private val matrixClient: MatrixClient,
    private val sendingQueue: SendQueues,
    private val logoutEntryPoint: LogoutEntryPoint,
    private val enableNativeSlidingSyncUseCase: EnableNativeSlidingSyncUseCase,
    snackbarDispatcher: SnackbarDispatcher,
) : BaseFlowNode<LoggedInFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Placeholder,
        savedStateMap = buildContext.savedStateMap,
    ),
    permanentNavModel = PermanentNavModel(
        navTargets = setOf(NavTarget.LoggedInPermanent),
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

    private var forceNativeSlidingSyncMigration: Boolean by mutableStateOf(false)

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                appNavigationStateService.onNavigateToSession(id, matrixClient.sessionId)
                // TODO We do not support Space yet, so directly navigate to main space
                appNavigationStateService.onNavigateToSpace(id, MAIN_SPACE)
                loggedInFlowProcessor.observeEvents(coroutineScope)

                ftueService.state
                    .onEach { ftueState ->
                        when (ftueState) {
                            is FtueState.Unknown -> Unit // Nothing to do
                            is FtueState.Incomplete -> backstack.safeRoot(NavTarget.Ftue)
                            is FtueState.Complete -> backstack.safeRoot(NavTarget.RoomList)
                        }
                    }
                    .launchIn(lifecycleScope)
            },
            onResume = {
                coroutineScope.launch {
                    // Force the user to log out if they were using the proxy sliding sync and it's no longer available, but native sliding sync is.
                    forceNativeSlidingSyncMigration = !matrixClient.isUsingNativeSlidingSync() &&
                        matrixClient.isNativeSlidingSyncSupported() &&
                        !matrixClient.isSlidingSyncProxySupported()
                }
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
        setupSendingQueue()
    }

    private fun setupSendingQueue() {
        sendingQueue.launchIn(lifecycleScope)
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
        data object Placeholder : NavTarget

        @Parcelize
        data object LoggedInPermanent : NavTarget

        @Parcelize
        data object RoomList : NavTarget

        @Parcelize
        data class Room(
            val roomIdOrAlias: RoomIdOrAlias,
            val serverNames: List<String> = emptyList(),
            val trigger: JoinedRoom.Trigger? = null,
            val roomDescription: RoomDescription? = null,
            val initialElement: RoomNavigationTarget = RoomNavigationTarget.Messages(),
            val targetId: UUID = UUID.randomUUID(),
        ) : NavTarget

        @Parcelize
        data class UserProfile(
            val userId: UserId,
        ) : NavTarget

        @Parcelize
        data class Settings(
            val initialElement: PreferencesEntryPoint.InitialTarget = PreferencesEntryPoint.InitialTarget.Root
        ) : NavTarget

        @Parcelize
        data object CreateRoom : NavTarget

        @Parcelize
        data class SecureBackup(
            val initialElement: SecureBackupEntryPoint.InitialTarget = SecureBackupEntryPoint.InitialTarget.Root
        ) : NavTarget

        @Parcelize
        data object Ftue : NavTarget

        @Parcelize
        data object RoomDirectorySearch : NavTarget

        @Parcelize
        data class IncomingShare(val intent: Intent) : NavTarget

        @Parcelize
        data object LogoutForNativeSlidingSyncMigrationNeeded : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Placeholder -> createNode<PlaceholderNode>(buildContext)
            NavTarget.LoggedInPermanent -> {
                val callback = object : LoggedInNode.Callback {
                    override fun navigateToNotificationTroubleshoot() {
                        backstack.push(NavTarget.Settings(PreferencesEntryPoint.InitialTarget.NotificationTroubleshoot))
                    }
                }
                createNode<LoggedInNode>(buildContext, listOf(callback))
            }
            NavTarget.RoomList -> {
                val callback = object : RoomListEntryPoint.Callback {
                    override fun onRoomClick(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias()))
                    }

                    override fun onSettingsClick() {
                        backstack.push(NavTarget.Settings())
                    }

                    override fun onCreateRoomClick() {
                        backstack.push(NavTarget.CreateRoom)
                    }

                    override fun onSetUpRecoveryClick() {
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.SetUpRecovery))
                    }

                    override fun onSessionConfirmRecoveryKeyClick() {
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey))
                    }

                    override fun onRoomSettingsClick(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias(), initialElement = RoomNavigationTarget.Details))
                    }

                    override fun onReportBugClick() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }

                    override fun onRoomDirectorySearchClick() {
                        backstack.push(NavTarget.RoomDirectorySearch)
                    }

                    override fun onLogoutForNativeSlidingSyncMigrationNeeded() {
                        backstack.push(NavTarget.LogoutForNativeSlidingSyncMigrationNeeded)
                    }
                }
                roomListEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            is NavTarget.Room -> {
                val callback = object : JoinedRoomLoadedFlowNode.Callback {
                    override fun onOpenRoom(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias()))
                    }

                    override fun onForwardedToSingleRoom(roomId: RoomId) {
                        coroutineScope.launch { attachRoom(roomId.toRoomIdOrAlias()) }
                    }

                    override fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                        when (data) {
                            is PermalinkData.UserLink -> {
                                // Should not happen (handled by MessagesNode)
                                Timber.e("User link clicked: ${data.userId}.")
                            }
                            is PermalinkData.RoomLink -> {
                                val target = NavTarget.Room(
                                    roomIdOrAlias = data.roomIdOrAlias,
                                    serverNames = data.viaParameters,
                                    trigger = JoinedRoom.Trigger.Timeline,
                                    initialElement = RoomNavigationTarget.Messages(data.eventId),
                                )
                                if (pushToBackstack) {
                                    backstack.push(target)
                                } else {
                                    backstack.replace(target)
                                }
                            }
                            is PermalinkData.FallbackLink,
                            is PermalinkData.RoomEmailInviteLink -> {
                                // Should not happen (handled by MessagesNode)
                            }
                        }
                    }

                    override fun onOpenGlobalNotificationSettings() {
                        backstack.push(NavTarget.Settings(PreferencesEntryPoint.InitialTarget.NotificationSettings))
                    }
                }
                val inputs = RoomFlowNode.Inputs(
                    roomIdOrAlias = navTarget.roomIdOrAlias,
                    roomDescription = Optional.ofNullable(navTarget.roomDescription),
                    serverNames = navTarget.serverNames,
                    trigger = Optional.ofNullable(navTarget.trigger),
                    initialElement = navTarget.initialElement
                )
                createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.UserProfile -> {
                val callback = object : UserProfileEntryPoint.Callback {
                    override fun onOpenRoom(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias()))
                    }
                }
                userProfileEntryPoint.nodeBuilder(this, buildContext)
                    .params(UserProfileEntryPoint.Params(userId = navTarget.userId))
                    .callback(callback)
                    .build()
            }
            is NavTarget.Settings -> {
                val callback = object : PreferencesEntryPoint.Callback {
                    override fun onOpenBugReport() {
                        plugins<Callback>().forEach { it.onOpenBugReport() }
                    }

                    override fun onSecureBackupClick() {
                        backstack.push(NavTarget.SecureBackup())
                    }

                    override fun onOpenRoomNotificationSettings(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias(), initialElement = RoomNavigationTarget.NotificationSettings))
                    }
                }
                val inputs = PreferencesEntryPoint.Params(navTarget.initialElement)
                preferencesEntryPoint.nodeBuilder(this, buildContext)
                    .params(inputs)
                    .callback(callback)
                    .build()
            }
            NavTarget.CreateRoom -> {
                val callback = object : CreateRoomEntryPoint.Callback {
                    override fun onSuccess(roomId: RoomId) {
                        backstack.replace(NavTarget.Room(roomId.toRoomIdOrAlias()))
                    }
                }

                createRoomEntryPoint
                    .nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }
            is NavTarget.SecureBackup -> {
                secureBackupEntryPoint.nodeBuilder(this, buildContext)
                    .params(SecureBackupEntryPoint.Params(initialElement = navTarget.initialElement))
                    .build()
            }
            NavTarget.Ftue -> {
                ftueEntryPoint.nodeBuilder(this, buildContext)
                    .build()
            }
            NavTarget.RoomDirectorySearch -> {
                roomDirectoryEntryPoint.nodeBuilder(this, buildContext)
                    .callback(object : RoomDirectoryEntryPoint.Callback {
                        override fun onResultClick(roomDescription: RoomDescription) {
                            backstack.push(
                                NavTarget.Room(
                                    roomIdOrAlias = roomDescription.roomId.toRoomIdOrAlias(),
                                    roomDescription = roomDescription,
                                    trigger = JoinedRoom.Trigger.RoomDirectory,
                                )
                            )
                        }
                    })
                    .build()
            }
            is NavTarget.IncomingShare -> {
                shareEntryPoint.nodeBuilder(this, buildContext)
                    .callback(object : ShareEntryPoint.Callback {
                        override fun onDone(roomIds: List<RoomId>) {
                            navigateUp()
                            if (roomIds.size == 1) {
                                val targetRoomId = roomIds.first()
                                backstack.push(NavTarget.Room(targetRoomId.toRoomIdOrAlias()))
                            }
                        }
                    })
                    .params(ShareEntryPoint.Params(intent = navTarget.intent))
                    .build()
            }
            is NavTarget.LogoutForNativeSlidingSyncMigrationNeeded -> {
                val callback = object : LogoutEntryPoint.Callback {
                    override fun onChangeRecoveryKeyClick() {
                        backstack.push(NavTarget.SecureBackup())
                    }
                }

                logoutEntryPoint.nodeBuilder(this, buildContext)
                    .onSuccessfulLogoutPendingAction {
                        enableNativeSlidingSyncUseCase()
                    }
                    .callback(callback)
                    .build()
            }
        }
    }

    suspend fun attachRoom(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String> = emptyList(),
        trigger: JoinedRoom.Trigger? = null,
        eventId: EventId? = null,
    ) {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.RoomList
        }
        attachChild<RoomFlowNode> {
            backstack.push(
                NavTarget.Room(
                    roomIdOrAlias = roomIdOrAlias,
                    serverNames = serverNames,
                    trigger = trigger,
                    initialElement = RoomNavigationTarget.Messages(
                        focusedEventId = eventId
                    )
                )
            )
        }
    }

    suspend fun attachUser(userId: UserId) {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.RoomList
        }
        attachChild<Node> {
            backstack.push(
                NavTarget.UserProfile(
                    userId = userId,
                )
            )
        }
    }

    internal suspend fun attachIncomingShare(intent: Intent) {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.RoomList
        }
        attachChild<Node> {
            backstack.push(
                NavTarget.IncomingShare(intent)
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            val ftueState by ftueService.state.collectAsState()
            BackstackView()
            if (ftueState is FtueState.Complete) {
                PermanentChild(permanentNavModel = permanentNavModel, navTarget = NavTarget.LoggedInPermanent)
            }

            // Set the force migration dialog here so it's always displayed over every screen
            if (forceNativeSlidingSyncMigration) {
                ForceNativeSlidingSyncMigrationDialog(onSubmit = {
                    // Enable native sliding sync if it wasn't already the case
                    enableNativeSlidingSyncUseCase()
                    // Then force the logout
                    coroutineScope.launch {
                        matrixClient.logout(userInitiated = true, ignoreSdkError = true)
                    }
                })
            }
        }
    }

    @ContributesNode(AppScope::class)
    class PlaceholderNode @AssistedInject constructor(
        @Assisted buildContext: BuildContext,
        @Assisted plugins: List<Plugin>,
    ) : Node(buildContext, plugins = plugins)
}

@Composable
private fun ForceNativeSlidingSyncMigrationDialog(
    onSubmit: () -> Unit,
) {
    ErrorDialog(
        title = null,
        content = stringResource(R.string.banner_migrate_to_native_sliding_sync_force_logout_title),
        submitText = stringResource(R.string.banner_migrate_to_native_sliding_sync_action),
        onSubmit = onSubmit,
        canDismiss = false,
    )
}

@PreviewsDayNight
@Composable
internal fun ForceNativeSlidingSyncMigrationDialogPreview() {
    ElementPreview {
        ForceNativeSlidingSyncMigrationDialog {}
    }
}
