/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.PermanentChild
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.NavElements
import com.bumble.appyx.core.navigation.NavKey
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStack.State.ACTIVE
import com.bumble.appyx.navmodel.backstack.BackStack.State.CREATED
import com.bumble.appyx.navmodel.backstack.BackStack.State.STASHED
import com.bumble.appyx.navmodel.backstack.BackStackElement
import com.bumble.appyx.navmodel.backstack.BackStackElements
import com.bumble.appyx.navmodel.backstack.operation.BackStackOperation
import com.bumble.appyx.navmodel.backstack.operation.Push
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.loggedin.LoggedInNode
import io.element.android.appnav.loggedin.MediaPreviewConfigMigration
import io.element.android.appnav.loggedin.SendQueues
import io.element.android.appnav.room.RoomFlowNode
import io.element.android.appnav.room.RoomNavigationTarget
import io.element.android.appnav.room.joined.JoinedRoomLoadedFlowNode
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.api.SessionEnterpriseService
import io.element.android.features.ftue.api.FtueEntryPoint
import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.home.api.HomeEntryPoint
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorContainer
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.features.roomdirectory.api.RoomDirectoryEntryPoint
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.features.startchat.api.StartChatEntryPoint
import io.element.android.features.userprofile.api.UserProfileEntryPoint
import io.element.android.features.verifysession.api.IncomingVerificationEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.waitForChildAttached
import io.element.android.libraries.architecture.waitForNavTargetAttached
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.theme.ElementThemeApp
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceListener
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.libraries.ui.common.nodes.emptyNode
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.watchers.AnalyticsRoomListStateWatcher
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

@ContributesNode(SessionScope::class)
@AssistedInject
class LoggedInFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val homeEntryPoint: HomeEntryPoint,
    private val preferencesEntryPoint: PreferencesEntryPoint,
    private val startChatEntryPoint: StartChatEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    private val secureBackupEntryPoint: SecureBackupEntryPoint,
    private val userProfileEntryPoint: UserProfileEntryPoint,
    private val ftueEntryPoint: FtueEntryPoint,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val ftueService: FtueService,
    private val roomDirectoryEntryPoint: RoomDirectoryEntryPoint,
    private val shareEntryPoint: ShareEntryPoint,
    private val matrixClient: MatrixClient,
    private val sendingQueue: SendQueues,
    private val incomingVerificationEntryPoint: IncomingVerificationEntryPoint,
    private val mediaPreviewConfigMigration: MediaPreviewConfigMigration,
    private val sessionEnterpriseService: SessionEnterpriseService,
    private val networkMonitor: NetworkMonitor,
    private val notificationConversationService: NotificationConversationService,
    private val syncService: SyncService,
    private val enterpriseService: EnterpriseService,
    private val appPreferencesStore: AppPreferencesStore,
    private val buildMeta: BuildMeta,
    snackbarDispatcher: SnackbarDispatcher,
    private val analyticsService: AnalyticsService,
    private val analyticsRoomListStateWatcher: AnalyticsRoomListStateWatcher,
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
        fun navigateToBugReport()
        fun navigateToAddAccount()
    }

    private val callback: Callback = callback()
    private val loggedInFlowProcessor = LoggedInEventProcessor(
        snackbarDispatcher = snackbarDispatcher,
        roomMembershipObserver = matrixClient.roomMembershipObserver,
    )

    private val verificationListener = object : SessionVerificationServiceListener {
        override fun onIncomingSessionRequest(verificationRequest: VerificationRequest.Incoming) {
            // Without this launch the rendering and actual state of this Appyx node's children gets out of sync, resulting in a crash.
            // This might be because this method is called back from Rust in a background thread.
            lifecycleScope.launch {
                val receivedAt = Instant.now()

                // Wait until the app is in foreground to display the incoming verification request
                appNavigationStateService.appNavigationState.first { it.isInForeground }

                // TODO there should also be a timeout for > 10 minutes elapsed since the request was created, but the SDK doesn't expose that info yet
                val now = Instant.now()
                val elapsedTimeSinceReceived = Duration.between(receivedAt, now).toKotlinDuration()

                // Discard the incoming verification request if it has timed out
                if (elapsedTimeSinceReceived > 2.minutes) {
                    Timber.w("Incoming verification request ${verificationRequest.details.flowId} discarded due to timeout.")
                    return@launch
                }

                // Wait for the RoomList UI to be ready so the incoming verification screen can be displayed on top of it
                // Otherwise, the RoomList UI may be incorrectly displayed on top
                withTimeout(5.seconds) {
                    backstack.elements.first { elements ->
                        elements.any { it.key.navTarget == NavTarget.Home }
                    }
                }

                backstack.singleTop(NavTarget.IncomingVerificationRequest(verificationRequest))
            }
        }
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycleScope.launch {
            sessionEnterpriseService.init()
        }
        lifecycle.subscribe(
            onCreate = {
                analyticsRoomListStateWatcher.start()
                appNavigationStateService.onNavigateToSession(id, matrixClient.sessionId)
                // TODO We do not support Space yet, so directly navigate to main space
                appNavigationStateService.onNavigateToSpace(id, MAIN_SPACE)
                loggedInFlowProcessor.observeEvents(sessionCoroutineScope)
                matrixClient.sessionVerificationService.setListener(verificationListener)
                mediaPreviewConfigMigration()

                sessionCoroutineScope.launch {
                    // Wait for the network to be connected before pre-fetching the max file upload size
                    networkMonitor.connectivity.first { networkStatus -> networkStatus == NetworkStatus.Connected }
                    matrixClient.getMaxFileUploadSize()
                }

                analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.FirstRoomsDisplayed)

                ftueService.state
                    .onEach { ftueState ->
                        when (ftueState) {
                            is FtueState.Unknown -> Unit // Nothing to do
                            is FtueState.Incomplete -> backstack.safeRoot(NavTarget.Ftue)
                            is FtueState.Complete -> backstack.safeRoot(NavTarget.Home)
                        }
                    }
                    .launchIn(lifecycleScope)
            },
            onResume = {
                lifecycleScope.launch {
                    val availableRoomIds = matrixClient.getJoinedRoomIds().getOrNull() ?: return@launch
                    notificationConversationService.onAvailableRoomsChanged(sessionId = matrixClient.sessionId, roomIds = availableRoomIds)
                }
            },
            onDestroy = {
                appNavigationStateService.onLeavingSpace(id)
                appNavigationStateService.onLeavingSession(id)
                loggedInFlowProcessor.stopObserving()
                matrixClient.sessionVerificationService.setListener(null)
                analyticsRoomListStateWatcher.stop()
            }
        )
        setupSendingQueue()
    }

    private fun setupSendingQueue() {
        sendingQueue.launchIn(lifecycleScope)
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Placeholder : NavTarget

        @Parcelize
        data object LoggedInPermanent : NavTarget

        @Parcelize
        data object Home : NavTarget

        @Parcelize
        data class Room(
            val roomIdOrAlias: RoomIdOrAlias,
            val serverNames: List<String> = emptyList(),
            val trigger: JoinedRoom.Trigger? = null,
            val roomDescription: RoomDescription? = null,
            val initialElement: RoomNavigationTarget = RoomNavigationTarget.Root(),
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
        data object RoomDirectory : NavTarget

        @Parcelize
        data class IncomingShare(val intent: Intent) : NavTarget

        @Parcelize
        data class IncomingVerificationRequest(val data: VerificationRequest.Incoming) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Placeholder -> emptyNode(buildContext)
            NavTarget.LoggedInPermanent -> {
                val callback = object : LoggedInNode.Callback {
                    override fun navigateToNotificationTroubleshoot() {
                        backstack.push(NavTarget.Settings(PreferencesEntryPoint.InitialTarget.NotificationTroubleshoot))
                    }
                }
                createNode<LoggedInNode>(buildContext, listOf(callback))
            }
            NavTarget.Home -> {
                val callback = object : HomeEntryPoint.Callback {
                    override fun navigateToRoom(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias()))
                    }

                    override fun navigateToSettings() {
                        backstack.push(NavTarget.Settings())
                    }

                    override fun navigateToCreateRoom() {
                        backstack.push(NavTarget.CreateRoom)
                    }

                    override fun navigateToSetUpRecovery() {
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.Root))
                    }

                    override fun navigateToEnterRecoveryKey() {
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey))
                    }

                    override fun navigateToRoomSettings(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias(), initialElement = RoomNavigationTarget.Details))
                    }

                    override fun navigateToBugReport() {
                        callback.navigateToBugReport()
                    }
                }
                homeEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.Room -> {
                val joinedRoomCallback = object : JoinedRoomLoadedFlowNode.Callback {
                    override fun navigateToRoom(roomId: RoomId, serverNames: List<String>) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias(), serverNames))
                    }

                    override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
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
                                    initialElement = RoomNavigationTarget.Root(data.eventId),
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

                    override fun navigateToGlobalNotificationSettings() {
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
                createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs, joinedRoomCallback))
            }
            is NavTarget.UserProfile -> {
                val callback = object : UserProfileEntryPoint.Callback {
                    override fun navigateToRoom(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias()))
                    }
                }
                userProfileEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = UserProfileEntryPoint.Params(userId = navTarget.userId),
                    callback = callback,
                )
            }
            is NavTarget.Settings -> {
                val callback = object : PreferencesEntryPoint.Callback {
                    override fun navigateToAddAccount() {
                        callback.navigateToAddAccount()
                    }

                    override fun navigateToBugReport() {
                        callback.navigateToBugReport()
                    }

                    override fun navigateToSecureBackup() {
                        backstack.push(NavTarget.SecureBackup())
                    }

                    override fun navigateToRoomNotificationSettings(roomId: RoomId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias(), initialElement = RoomNavigationTarget.NotificationSettings))
                    }

                    override fun navigateToEvent(roomId: RoomId, eventId: EventId) {
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias(), initialElement = RoomNavigationTarget.Root(eventId)))
                    }
                }
                val inputs = PreferencesEntryPoint.Params(navTarget.initialElement)
                preferencesEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = inputs,
                    callback = callback,
                )
            }
            NavTarget.CreateRoom -> {
                val callback = object : StartChatEntryPoint.Callback {
                    override fun onRoomCreated(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) {
                        backstack.replace(NavTarget.Room(roomIdOrAlias = roomIdOrAlias, serverNames = serverNames))
                    }

                    override fun navigateToRoomDirectory() {
                        backstack.push(NavTarget.RoomDirectory)
                    }
                }

                startChatEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.SecureBackup -> {
                secureBackupEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = SecureBackupEntryPoint.Params(initialElement = navTarget.initialElement),
                    callback = object : SecureBackupEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }
                    },
                )
            }
            NavTarget.Ftue -> {
                ftueEntryPoint.createNode(this, buildContext)
            }
            NavTarget.RoomDirectory -> {
                roomDirectoryEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = object : RoomDirectoryEntryPoint.Callback {
                        override fun navigateToRoom(roomDescription: RoomDescription) {
                            backstack.push(
                                NavTarget.Room(
                                    roomIdOrAlias = roomDescription.roomId.toRoomIdOrAlias(),
                                    roomDescription = roomDescription,
                                    trigger = JoinedRoom.Trigger.RoomDirectory,
                                )
                            )
                        }
                    },
                )
            }
            is NavTarget.IncomingShare -> {
                shareEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = ShareEntryPoint.Params(intent = navTarget.intent),
                    callback = object : ShareEntryPoint.Callback {
                        override fun onDone(roomIds: List<RoomId>) {
                            backstack.pop()
                            roomIds.singleOrNull()?.let { roomId ->
                                backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias()))
                            }
                        }
                    },
                )
            }
            is NavTarget.IncomingVerificationRequest -> {
                incomingVerificationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = IncomingVerificationEntryPoint.Params(navTarget.data),
                    callback = object : IncomingVerificationEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }
                    },
                )
            }
        }
    }

    suspend fun attachRoom(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String> = emptyList(),
        trigger: JoinedRoom.Trigger? = null,
        eventId: EventId? = null,
        clearBackstack: Boolean,
    ): RoomFlowNode {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.Home
        }
        attachChild<RoomFlowNode> {
            val roomNavTarget = NavTarget.Room(
                roomIdOrAlias = roomIdOrAlias,
                serverNames = serverNames,
                trigger = trigger,
                initialElement = RoomNavigationTarget.Root(eventId = eventId)
            )
            backstack.accept(AttachRoomOperation(roomNavTarget, clearBackstack))
        }

        // If we don't do this check, we might be returning while a previous node with the same type is still displayed
        // This means we may attach some new nodes to that one, which will be quickly replaced by the one instantiated above
        return waitForChildAttached<RoomFlowNode, NavTarget> {
            it is NavTarget.Room &&
                it.roomIdOrAlias == roomIdOrAlias &&
                it.initialElement is RoomNavigationTarget.Root &&
                it.initialElement.eventId == eventId
        }
    }

    suspend fun attachUser(userId: UserId) {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.Home
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
            navTarget is NavTarget.Home
        }
        attachChild<Node> {
            backstack.push(
                NavTarget.IncomingShare(intent)
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val colors by remember {
            enterpriseService.semanticColorsFlow(sessionId = matrixClient.sessionId)
        }.collectAsState(SemanticColorsLightDark.default)
        ElementThemeApp(
            appPreferencesStore = appPreferencesStore,
            compoundLight = colors.light,
            compoundDark = colors.dark,
            buildMeta = buildMeta,
        ) {
            val isOnline by syncService.isOnline.collectAsState()
            ConnectivityIndicatorContainer(
                isOnline = isOnline,
                modifier = modifier,
            ) { contentModifier ->
                Box(modifier = contentModifier) {
                    val ftueState by ftueService.state.collectAsState()
                    BackstackView()
                    if (ftueState is FtueState.Complete) {
                        PermanentChild(permanentNavModel = permanentNavModel, navTarget = NavTarget.LoggedInPermanent)
                    }
                }
            }
        }
    }
}

@Parcelize
private class AttachRoomOperation(
    val roomTarget: LoggedInFlowNode.NavTarget.Room,
    val clearBackstack: Boolean,
) : BackStackOperation<LoggedInFlowNode.NavTarget> {
    override fun isApplicable(elements: NavElements<LoggedInFlowNode.NavTarget, BackStack.State>) = true

    override fun invoke(elements: BackStackElements<LoggedInFlowNode.NavTarget>): BackStackElements<LoggedInFlowNode.NavTarget> {
        return if (clearBackstack) {
            // Makes sure the room list target is alone in the backstack and stashed
            elements.mapNotNull { element ->
                if (element.key.navTarget == LoggedInFlowNode.NavTarget.Home) {
                    element.transitionTo(STASHED, this)
                } else {
                    null
                }
            } + BackStackElement(
                key = NavKey(roomTarget),
                fromState = CREATED,
                targetState = ACTIVE,
                operation = this
            )
        } else {
            Push<LoggedInFlowNode.NavTarget>(roomTarget).invoke(elements)
        }
    }
}
