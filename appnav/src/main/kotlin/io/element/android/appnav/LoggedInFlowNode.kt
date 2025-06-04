/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import android.content.Intent
import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.bumble.appyx.core.plugin.plugins
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
import io.element.android.features.preferences.api.PreferencesEntryPoint
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.features.roomdirectory.api.RoomDirectoryEntryPoint
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.features.share.api.ShareEntryPoint
import io.element.android.features.userprofile.api.UserProfileEntryPoint
import io.element.android.features.verifysession.api.IncomingVerificationEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.waitForNavTargetAttached
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.MAIN_SPACE
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceListener
import io.element.android.libraries.matrix.api.verification.VerificationRequest
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
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val ftueService: FtueService,
    private val roomDirectoryEntryPoint: RoomDirectoryEntryPoint,
    private val shareEntryPoint: ShareEntryPoint,
    private val matrixClient: MatrixClient,
    private val sendingQueue: SendQueues,
    private val logoutEntryPoint: LogoutEntryPoint,
    private val incomingVerificationEntryPoint: IncomingVerificationEntryPoint,
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

    private val loggedInFlowProcessor = LoggedInEventProcessor(
        snackbarDispatcher,
        matrixClient.roomMembershipObserver(),
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
                        elements.any { it.key.navTarget == NavTarget.RoomList }
                    }
                }

                backstack.singleTop(NavTarget.IncomingVerificationRequest(verificationRequest))
            }
        }
    }

    override fun onBuilt() {
        super.onBuilt()

        lifecycle.subscribe(
            onCreate = {
                appNavigationStateService.onNavigateToSession(id, matrixClient.sessionId)
                // TODO We do not support Space yet, so directly navigate to main space
                appNavigationStateService.onNavigateToSpace(id, MAIN_SPACE)
                loggedInFlowProcessor.observeEvents(sessionCoroutineScope)
                matrixClient.sessionVerificationService().setListener(verificationListener)

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
            onDestroy = {
                appNavigationStateService.onLeavingSpace(id)
                appNavigationStateService.onLeavingSession(id)
                loggedInFlowProcessor.stopObserving()
                matrixClient.sessionVerificationService().setListener(null)
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

        @Parcelize
        data class IncomingVerificationRequest(val data: VerificationRequest.Incoming) : NavTarget
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
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.Root))
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
                        sessionCoroutineScope.launch { attachRoom(roomId.toRoomIdOrAlias(), clearBackstack = false) }
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

                    override fun navigateTo(sessionId: SessionId, roomId: RoomId, eventId: EventId) {
                        // We do not check the sessionId, but it will have to be done at some point (multi account)
                        if (sessionId != matrixClient.sessionId) {
                            Timber.e("SessionId mismatch, expected ${matrixClient.sessionId} but got $sessionId")
                        }
                        backstack.push(NavTarget.Room(roomId.toRoomIdOrAlias(), initialElement = RoomNavigationTarget.Messages(eventId)))
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
                    override fun onOpenRoom(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) {
                        backstack.replace(NavTarget.Room(roomIdOrAlias = roomIdOrAlias, serverNames = serverNames))
                    }

                    override fun onOpenRoomDirectory() {
                        backstack.push(NavTarget.RoomDirectorySearch)
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
                    .callback(object : SecureBackupEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }
                    })
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
                    .callback(callback)
                    .build()
            }
            is NavTarget.IncomingVerificationRequest -> {
                incomingVerificationEntryPoint.nodeBuilder(this, buildContext)
                    .params(IncomingVerificationEntryPoint.Params(navTarget.data))
                    .callback(object : IncomingVerificationEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }
                    })
                    .build()
            }
        }
    }

    suspend fun attachRoom(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String> = emptyList(),
        trigger: JoinedRoom.Trigger? = null,
        eventId: EventId? = null,
        clearBackstack: Boolean,
    ) {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.RoomList
        }
        attachChild<RoomFlowNode> {
            val roomNavTarget = NavTarget.Room(
                roomIdOrAlias = roomIdOrAlias,
                serverNames = serverNames,
                trigger = trigger,
                initialElement = RoomNavigationTarget.Messages(
                    focusedEventId = eventId
                )
            )
            backstack.accept(AttachRoomOperation(roomNavTarget, clearBackstack))
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
        }
    }

    @ContributesNode(AppScope::class)
    class PlaceholderNode @AssistedInject constructor(
        @Assisted buildContext: BuildContext,
        @Assisted plugins: List<Plugin>,
    ) : Node(buildContext, plugins = plugins)
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
                if (element.key.navTarget == LoggedInFlowNode.NavTarget.RoomList) {
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
