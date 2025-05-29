/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.api.gateway.PushGatewayFailure
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ContributesMultibinding(AppScope::class)
class PushLoopbackTest @Inject constructor(
    private val pushService: PushService,
    private val diagnosticPushHandler: DiagnosticPushHandler,
    private val clock: SystemClock,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 500
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_description),
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val startTime = clock.epochMillis()
        val completable = CompletableDeferred<Long>()
        val job = coroutineScope.launch {
            diagnosticPushHandler.state.first()
            completable.complete(clock.epochMillis() - startTime)
        }
        val testPushResult = try {
            pushService.testPush()
        } catch (pusherRejected: PushGatewayFailure.PusherRejected) {
            val hasQuickFix = pushService.getCurrentPushProvider()?.canRotateToken() == true
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_failure_1),
                status = NotificationTroubleshootTestState.Status.Failure(hasQuickFix)
            )
            job.cancel()
            return
        } catch (e: Exception) {
            Timber.e(e, "Failed to test push")
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_failure_2, e.message),
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
            job.cancel()
            return
        }
        if (!testPushResult) {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_failure_3),
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
            job.cancel()
            return
        }
        @Suppress("RunCatchingNotAllowed")
        runCatching {
            withTimeout(10.seconds) {
                completable.await()
            }
        }.fold(
            onSuccess = { duration ->
                delegate.updateState(
                    description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_success, duration),
                    status = NotificationTroubleshootTestState.Status.Success
                )
            },
            onFailure = {
                job.cancel()
                delegate.updateState(
                    description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_failure_4),
                    status = NotificationTroubleshootTestState.Status.Failure(false)
                )
            }
        )
    }

    override suspend fun quickFix(coroutineScope: CoroutineScope) {
        delegate.start()
        pushService.getCurrentPushProvider()?.rotateToken()
        run(coroutineScope)
    }

    override suspend fun reset() = delegate.reset()
}
