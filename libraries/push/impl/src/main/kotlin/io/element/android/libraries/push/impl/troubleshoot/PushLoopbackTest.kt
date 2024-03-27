/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.push.impl.troubleshoot

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.notifications.NotificationTroubleshootTest
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestDelegate
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.api.gateway.PushGatewayFailure
import io.element.android.libraries.push.impl.R
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_failure_1),
                status = NotificationTroubleshootTestState.Status.Failure(false)
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
        val result = withTimeoutOrNull(10.seconds) {
            completable.await()
        }
        job.cancel()
        if (result == null) {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_failure_4),
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_push_loop_back_success, result),
                status = NotificationTroubleshootTestState.Status.Success
            )
        }
    }

    override suspend fun reset() = delegate.reset()
}
