/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.call.api.ElementCallController
import io.element.android.call.api.ElementCallNotificationConfig
import io.element.android.call.api.ElementCallOptions
import io.element.android.call.api.rtc.MatrixRtcLogLevel
import io.element.android.call.api.rtc.MatrixRtcLoggingConfiguration
import io.element.android.call.impl.ElementCallStack
import io.element.android.features.call.api.CurrentCallTracker
import io.element.android.features.call.api.RingingCallTracker
import io.element.android.features.callnative.api.ElementCallTransportFactory
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * The native call controller of a session, built on demand.
 *
 * An interface so that what starts calls can be tested without the real stack, which loads the RTC
 * native library.
 */
interface ElementCallControllers {
    /**
     * The session's controller, building its call stack on first use. Null when there is no such
     * session - logged out, or a session id that cannot be restored.
     */
    suspend fun getOrBuild(sessionId: SessionId): ElementCallController?

    /**
     * The session's controller once its stack exists, and null until then. Never builds one.
     *
     * A flow rather than a getter because the stack appears part-way through the session's life, when
     * a call is first placed or answered, and whatever draws the call has to notice when it does.
     */
    fun controller(sessionId: SessionId): Flow<ElementCallController?>

    /**
     * The controller of whichever session currently has a call, or null when none does.
     *
     * For the Activity-level seams, which are app-scoped and have no session of their own to ask. Only
     * sessions whose stack is already built are considered, so asking cannot start one.
     */
    fun withRunningCall(): ElementCallController?
}

/**
 * Builds one call stack per session, from a Matrix client, and keeps it for the session's life.
 *
 * App-scoped and keyed by session id rather than session-scoped, because everything that starts a call
 * is app-scoped: the push path that resolves a ringing call, the notification it posts, and the Activity
 * that answers it all run on a [MatrixClient] fetched by id, and none of them has a session graph. A
 * session-scoped stack could only be reached after the user had navigated into the session, so answering
 * a call notification after a cold start would find nothing to answer with.
 *
 * One stack serves every call in a session; each call opens and closes its own room underneath, which
 * the component does itself.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultElementCallControllers(
    @ApplicationContext private val context: Context,
    private val matrixClientProvider: MatrixClientProvider,
    private val transportFactory: ElementCallTransportFactory,
    private val currentCallTracker: CurrentCallTracker,
    private val ringingCallTracker: RingingCallTracker,
    private val appForegroundStateService: AppForegroundStateService,
    private val appPreferencesStore: AppPreferencesStore,
    private val featureFlagService: FeatureFlagService,
) : ElementCallControllers {
    // A flow rather than a plain map because what draws the call has to see the stack appear.
    private val stacks = MutableStateFlow<Map<SessionId, ElementCallStack>>(emptyMap())

    // One at a time across every session: two calls placed at once must not race into two stacks for
    // the same session, because only one of them would ever be closed.
    private val mutex = Mutex()

    override suspend fun getOrBuild(sessionId: SessionId): ElementCallController? = mutex.withLock {
        stacks.value[sessionId]?.let { return@withLock it.controller }
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull()
        if (client == null) {
            Timber.w("NativeCall: no client for $sessionId, cannot start a call")
            return@withLock null
        }
        build(client).also { stack ->
            stacks.update { it + (sessionId to stack) }
            // The client's scope is the stack's lifetime: the component has no shutdown of its own, so
            // cancelling this at logout is what stops the RTC core. Closing here only releases the
            // component's own reference, so a dead stack is not left behind it.
            client.sessionCoroutineScope.coroutineContext.job.invokeOnCompletion {
                stacks.update { if (it[sessionId] === stack) it - sessionId else it }
                stack.close()
            }
        }.controller
    }

    override fun controller(sessionId: SessionId): Flow<ElementCallController?> =
        stacks.map { it[sessionId]?.controller }.distinctUntilChanged()

    override fun withRunningCall(): ElementCallController? =
        stacks.value.values.firstNotNullOfOrNull { stack ->
            stack.controller.takeIf { it.state.value != null }
        }

    private suspend fun build(client: MatrixClient): ElementCallStack =
        ElementCallStack.Builder(context, transportFactory.create(client))
            .lifecycleListener(
                ElementXCallLifecycleListener(
                    sessionId = client.sessionId,
                    currentCallTracker = currentCallTracker,
                    ringingCallTracker = ringingCallTracker,
                    sessionCoroutineScope = client.sessionCoroutineScope,
                    appForegroundStateService = appForegroundStateService,
                )
            )
            .roomContext(ElementXRoomContextProvider(client))
            .options(options())
            .build(scope = client.sessionCoroutineScope)

    private suspend fun options() = ElementCallOptions(
        logging = loggingConfiguration(),
        notification = ElementCallNotificationConfig(
            // The same id the WebView call's foreground service uses, so that the two paths cannot
            // leave two ongoing-call notifications in the shade.
            notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.ONGOING_CALL),
        ),
    )

    /**
     * Read here rather than in `PlatformInitializer`, where the Rust SDK's tracing is configured: the
     * core applies this when it first loads, which is when a call is first placed, and this is the first
     * place where both the preference and the flag are readable.
     */
    private suspend fun loggingConfiguration(): MatrixRtcLoggingConfiguration {
        val logLevel = appPreferencesStore.getTracingLogLevelFlow().first()
        return MatrixRtcLoggingConfiguration(
            // Floored at debug: turning the SDK level down to quiet the timeline and event-cache
            // traces is exactly what you do while working on calls, and it must not also silence the
            // RTC core. The core's own filter holds down the parts that log per frame.
            logLevel = maxOf(logLevel, LogLevel.DEBUG).toMatrixRtcLogLevel(),
            writesToLogcat = featureFlagService.isFeatureEnabled(FeatureFlags.PrintLogsToLogcat),
        )
    }

    private fun LogLevel.toMatrixRtcLogLevel(): MatrixRtcLogLevel = when (this) {
        LogLevel.ERROR -> MatrixRtcLogLevel.ERROR
        LogLevel.WARN -> MatrixRtcLogLevel.WARN
        LogLevel.INFO -> MatrixRtcLogLevel.INFO
        LogLevel.DEBUG -> MatrixRtcLogLevel.DEBUG
        LogLevel.TRACE -> MatrixRtcLogLevel.TRACE
    }
}
