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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.state.MutableSavedStateMap
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.di.MatrixSessionCache
import io.element.android.appnav.intent.IntentResolver
import io.element.android.appnav.intent.ResolvedIntent
import io.element.android.appnav.room.RoomFlowNode
import io.element.android.appnav.root.RootNavStateFlowFactory
import io.element.android.appnav.root.RootPresenter
import io.element.android.appnav.root.RootView
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.login.api.LoginParams
import io.element.android.features.login.api.accesscontrol.AccountProviderAccessControl
import io.element.android.features.rageshake.api.bugreport.BugReportEntryPoint
import io.element.android.features.signedout.api.SignedOutEntryPoint
import io.element.android.libraries.accountselect.api.AccountSelectEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.appyx.rememberDelegateTransitionHandler
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.waitForChildAttached
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.deeplink.api.DeeplinkData
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import io.element.android.libraries.sessionstorage.api.LoggedInState
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.ui.common.nodes.emptyNode
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.watchers.AnalyticsColdStartWatcher
import io.element.android.services.appnavstate.api.ROOM_OPENED_FROM_NOTIFICATION
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(AppScope::class)
@AssistedInject
class RootFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val sessionStore: SessionStore,
    private val accountProviderAccessControl: AccountProviderAccessControl,
    private val navStateFlowFactory: RootNavStateFlowFactory,
    private val matrixSessionCache: MatrixSessionCache,
    private val presenter: RootPresenter,
    private val bugReportEntryPoint: BugReportEntryPoint,
    private val signedOutEntryPoint: SignedOutEntryPoint,
    private val accountSelectEntryPoint: AccountSelectEntryPoint,
    private val intentResolver: IntentResolver,
    private val oidcActionFlow: OidcActionFlow,
    private val featureFlagService: FeatureFlagService,
    private val announcementService: AnnouncementService,
    private val analyticsService: AnalyticsService,
    private val analyticsColdStartWatcher: AnalyticsColdStartWatcher,
) : BaseFlowNode<RootFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.SplashScreen,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    override fun onBuilt() {
        analyticsColdStartWatcher.start()
        matrixSessionCache.restoreWithSavedState(buildContext.savedStateMap)
        super.onBuilt()
        observeNavState()
    }

    override fun onSaveInstanceState(state: MutableSavedStateMap) {
        super.onSaveInstanceState(state)
        matrixSessionCache.saveIntoSavedState(state)
        navStateFlowFactory.saveIntoSavedState(state)
    }

    private fun observeNavState() {
        navStateFlowFactory.create(buildContext.savedStateMap).distinctUntilChanged().onEach { navState ->
            Timber.v("navState=$navState")
            when (navState.loggedInState) {
                is LoggedInState.LoggedIn -> {
                    if (navState.loggedInState.isTokenValid) {
                        tryToRestoreLatestSession(
                            onSuccess = { sessionId -> switchToLoggedInFlow(sessionId, navState.cacheIndex) },
                            onFailure = { switchToNotLoggedInFlow(null) }
                        )
                    } else {
                        switchToSignedOutFlow(SessionId(navState.loggedInState.sessionId))
                    }
                }
                LoggedInState.NotLoggedIn -> {
                    switchToNotLoggedInFlow(null)
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun switchToLoggedInFlow(sessionId: SessionId, navId: Int) {
        backstack.safeRoot(NavTarget.LoggedInFlow(sessionId, navId))
    }

    private fun switchToNotLoggedInFlow(params: LoginParams?) {
        matrixSessionCache.removeAll()
        backstack.safeRoot(NavTarget.NotLoggedInFlow(params))
    }

    private fun switchToSignedOutFlow(sessionId: SessionId) {
        backstack.safeRoot(NavTarget.SignedOutFlow(sessionId))
    }

    private suspend fun restoreSessionIfNeeded(
        sessionId: SessionId,
        onFailure: () -> Unit,
        onSuccess: (SessionId) -> Unit,
    ) {
        matrixSessionCache.getOrRestore(sessionId).onSuccess {
            Timber.v("Succeed to restore session $sessionId")
            onSuccess(sessionId)
        }.onFailure {
            Timber.e(it, "Failed to restore session $sessionId")
            onFailure()
        }
    }

    private suspend fun tryToRestoreLatestSession(
        onSuccess: (SessionId) -> Unit, onFailure: () -> Unit
    ) {
        val latestSessionId = sessionStore.getLatestSessionId()
        if (latestSessionId == null) {
            onFailure()
            return
        }
        restoreSessionIfNeeded(latestSessionId, onFailure, onSuccess)
    }

    private fun onOpenBugReport() {
        backstack.push(NavTarget.BugReport)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RootView(
            state = state,
            modifier = modifier,
            onOpenBugReport = this::onOpenBugReport,
        ) {
            val backstackSlider = rememberBackstackSlider<NavTarget>(
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
            )
            val backstackFader = rememberBackstackFader<NavTarget>(
                transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
            )
            val transitionHandler = rememberDelegateTransitionHandler<NavTarget, BackStack.State> { navTarget ->
                when (navTarget) {
                    is NavTarget.SplashScreen,
                    is NavTarget.LoggedInFlow -> backstackFader
                    else -> backstackSlider
                }
            }
            BackstackView(transitionHandler = transitionHandler)
            announcementService.Render(Modifier)
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize data object SplashScreen : NavTarget

        @Parcelize data class AccountSelect(
            val currentSessionId: SessionId,
            val intent: Intent?,
            val permalinkData: PermalinkData?,
        ) : NavTarget

        @Parcelize data class NotLoggedInFlow(
            val params: LoginParams?
        ) : NavTarget

        @Parcelize data class LoggedInFlow(
            val sessionId: SessionId, val navId: Int
        ) : NavTarget

        @Parcelize data class SignedOutFlow(
            val sessionId: SessionId
        ) : NavTarget

        @Parcelize data object BugReport : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.LoggedInFlow -> {
                val matrixClient = matrixSessionCache.getOrNull(navTarget.sessionId)
                    ?: return emptyNode(buildContext).also {
                        Timber.w("Couldn't find any session, go through SplashScreen")
                    }
                val inputs = LoggedInAppScopeFlowNode.Inputs(matrixClient)
                val callback = object : LoggedInAppScopeFlowNode.Callback {
                    override fun navigateToBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }

                    override fun navigateToAddAccount() {
                        backstack.push(NavTarget.NotLoggedInFlow(null))
                    }
                }
                createNode<LoggedInAppScopeFlowNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.NotLoggedInFlow -> {
                val callback = object : NotLoggedInFlowNode.Callback {
                    override fun navigateToBugReport() {
                        backstack.push(NavTarget.BugReport)
                    }

                    override fun onDone() {
                        backstack.pop()
                    }
                }
                val params = NotLoggedInFlowNode.Params(
                    loginParams = navTarget.params,
                )
                createNode<NotLoggedInFlowNode>(buildContext, plugins = listOf(params, callback))
            }
            is NavTarget.SignedOutFlow -> {
                signedOutEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = SignedOutEntryPoint.Params(
                        sessionId = navTarget.sessionId,
                    ),
                )
            }
            NavTarget.SplashScreen -> emptyNode(buildContext)
            NavTarget.BugReport -> {
                val callback = object : BugReportEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                bugReportEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.AccountSelect -> {
                val callback: AccountSelectEntryPoint.Callback = object : AccountSelectEntryPoint.Callback {
                    override fun onAccountSelected(sessionId: SessionId) {
                        lifecycleScope.launch {
                            if (sessionId == navTarget.currentSessionId) {
                                // Ensure that the account selection Node is removed from the backstack
                                // Do not pop when the account is changed to avoid a UI flicker.
                                backstack.pop()
                            }
                            attachSession(sessionId).apply {
                                if (navTarget.intent != null) {
                                    attachIncomingShare(navTarget.intent)
                                } else if (navTarget.permalinkData != null) {
                                    attachPermalinkData(navTarget.permalinkData)
                                }
                            }
                        }
                    }

                    override fun onCancel() {
                        backstack.pop()
                    }
                }
                accountSelectEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
        }
    }

    suspend fun handleIntent(intent: Intent) {
        val resolvedIntent = intentResolver.resolve(intent) ?: return
        when (resolvedIntent) {
            is ResolvedIntent.Navigation -> {
                val openingRoomFromNotification = intent.getBooleanExtra(ROOM_OPENED_FROM_NOTIFICATION, false)
                if (openingRoomFromNotification && resolvedIntent.deeplinkData is DeeplinkData.Room) {
                    analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.NotificationTapOpensTimeline)
                }
                navigateTo(resolvedIntent.deeplinkData)
            }
            is ResolvedIntent.Login -> onLoginLink(resolvedIntent.params)
            is ResolvedIntent.Oidc -> onOidcAction(resolvedIntent.oidcAction)
            is ResolvedIntent.Permalink -> navigateTo(resolvedIntent.permalinkData)
            is ResolvedIntent.IncomingShare -> onIncomingShare(resolvedIntent.intent)
        }
    }

    private suspend fun onLoginLink(params: LoginParams) {
        if (accountProviderAccessControl.isAllowedToConnectToAccountProvider(params.accountProvider.ensureProtocol())) {
            // Is there a session already?
            val sessions = sessionStore.getAllSessions()
            if (sessions.isNotEmpty()) {
                if (featureFlagService.isFeatureEnabled(FeatureFlags.MultiAccount)) {
                    val loginHintMatrixId = params.loginHint?.removePrefix("mxid:")
                    val existingAccount = sessions.find { it.userId == loginHintMatrixId }
                    if (existingAccount != null) {
                        // We have an existing account matching the login hint, ensure this is the current session
                        sessionStore.setLatestSession(existingAccount.userId)
                    } else {
                        val latestSessionId = sessions.maxBy { it.lastUsageIndex }.userId
                        attachSession(SessionId(latestSessionId))
                        backstack.push(NavTarget.NotLoggedInFlow(params))
                    }
                } else {
                    Timber.w("Login link ignored, multi account is disabled")
                }
            } else {
                switchToNotLoggedInFlow(params)
            }
        } else {
            Timber.w("Login link ignored, we are not allowed to connect to the homeserver")
        }
    }

    private suspend fun onIncomingShare(intent: Intent) {
        // Is there a session already?
        val latestSessionId = sessionStore.getLatestSessionId()
        if (latestSessionId == null) {
            // No session, open login
            switchToNotLoggedInFlow(null)
        } else {
            // wait for the current session to be restored
            val loggedInFlowNode = attachSession(latestSessionId)
            if (sessionStore.numberOfSessions() > 1) {
                // Several accounts, let the user choose which one to use
                backstack.push(
                    NavTarget.AccountSelect(
                        currentSessionId = latestSessionId,
                        intent = intent,
                        permalinkData = null,
                    )
                )
            } else {
                // Only one account, directly attach the incoming share node.
                loggedInFlowNode.attachIncomingShare(intent)
            }
        }
    }

    private suspend fun navigateTo(permalinkData: PermalinkData) {
        Timber.d("Navigating to $permalinkData")
        // Is there a session already?
        val latestSessionId = sessionStore.getLatestSessionId()
        if (latestSessionId == null) {
            // No session, open login
            switchToNotLoggedInFlow(null)
        } else {
            // wait for the current session to be restored
            val loggedInFlowNode = attachSession(latestSessionId)
            when (permalinkData) {
                is PermalinkData.FallbackLink -> Unit
                is PermalinkData.RoomEmailInviteLink -> Unit
                else -> {
                    if (sessionStore.numberOfSessions() > 1) {
                        // Several accounts, let the user choose which one to use
                        backstack.push(
                            NavTarget.AccountSelect(
                                currentSessionId = latestSessionId,
                                intent = null,
                                permalinkData = permalinkData,
                            )
                        )
                    } else {
                        // Only one account, directly attach the room or the user node.
                        loggedInFlowNode.attachPermalinkData(permalinkData)
                    }
                }
            }
        }
    }

    private suspend fun LoggedInFlowNode.attachPermalinkData(permalinkData: PermalinkData) {
        when (permalinkData) {
            is PermalinkData.FallbackLink -> Unit
            is PermalinkData.RoomEmailInviteLink -> Unit
            is PermalinkData.RoomLink -> {
                // If there is a thread id, focus on it in the main timeline
                val focusedEventId = if (permalinkData.threadId != null) {
                    permalinkData.threadId?.asEventId()
                } else {
                    permalinkData.eventId
                }
                attachRoom(
                    roomIdOrAlias = permalinkData.roomIdOrAlias,
                    trigger = JoinedRoom.Trigger.MobilePermalink,
                    serverNames = permalinkData.viaParameters,
                    eventId = focusedEventId,
                    clearBackstack = true
                ).maybeAttachThread(permalinkData.threadId, permalinkData.eventId)
            }
            is PermalinkData.UserLink -> {
                attachUser(permalinkData.userId)
            }
        }
    }

    private suspend fun RoomFlowNode.maybeAttachThread(threadId: ThreadId?, focusedEventId: EventId?) {
        if (threadId != null) {
            attachThread(threadId, focusedEventId)
        }
    }

    private suspend fun navigateTo(deeplinkData: DeeplinkData) {
        Timber.d("Navigating to $deeplinkData")
        attachSession(deeplinkData.sessionId).let { loggedInFlowNode ->
            when (deeplinkData) {
                is DeeplinkData.Root -> Unit // The room list will always be shown, observing FtueState
                is DeeplinkData.Room -> {
                    loggedInFlowNode.attachRoom(
                        roomIdOrAlias = deeplinkData.roomId.toRoomIdOrAlias(),
                        eventId = if (deeplinkData.threadId != null) deeplinkData.threadId?.asEventId() else deeplinkData.eventId,
                        clearBackstack = true,
                    ).maybeAttachThread(deeplinkData.threadId, deeplinkData.eventId)
                }
            }
        }
    }

    private fun onOidcAction(oidcAction: OidcAction) {
        oidcActionFlow.post(oidcAction)
    }

    private suspend fun attachSession(sessionId: SessionId): LoggedInFlowNode {
        // Ensure that the session is the latest one
        sessionStore.setLatestSession(sessionId.value)
        return waitForChildAttached<LoggedInAppScopeFlowNode, NavTarget> { navTarget ->
            navTarget is NavTarget.LoggedInFlow && navTarget.sessionId == sessionId
        }.attachSession()
    }
}

private suspend fun SessionStore.getLatestSessionId() = getLatestSession()?.userId?.let(::SessionId)
