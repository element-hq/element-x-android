/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.NotificationDisplayer
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ContributesMultibinding(AppScope::class)
class NotificationTest @Inject constructor(
    private val notificationCreator: NotificationCreator,
    private val notificationDisplayer: NotificationDisplayer,
    private val notificationClickHandler: NotificationClickHandler,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 50
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_description),
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val notification = notificationCreator.createDiagnosticNotification()
        val result = notificationDisplayer.displayDiagnosticNotification(notification)
        if (result) {
            coroutineScope.listenToNotificationClick()
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_waiting),
                status = NotificationTroubleshootTestState.Status.WaitingForUser
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_permission_failure),
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
        }
    }

    private fun CoroutineScope.listenToNotificationClick() = launch {
        val job = launch {
            notificationClickHandler.state.first()
            Timber.d("Notification clicked!")
        }
        runCatching {
            withTimeout(30.seconds) {
                job.join()
            }
        }.fold(
            onSuccess = {
                delegate.updateState(
                    description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_success),
                    status = NotificationTroubleshootTestState.Status.Success
                )
            },
            onFailure = {
                job.cancel()
                notificationDisplayer.dismissDiagnosticNotification()
                delegate.updateState(
                    description = stringProvider.getString(R.string.troubleshoot_notifications_test_display_notification_failure),
                    status = NotificationTroubleshootTestState.Status.Failure(false)
                )
            }
        )
    }.invokeOnCompletion {
        // Ensure that the notification is cancelled when the screen is left
        notificationDisplayer.dismissDiagnosticNotification()
    }

    override suspend fun reset() = delegate.reset()
}
