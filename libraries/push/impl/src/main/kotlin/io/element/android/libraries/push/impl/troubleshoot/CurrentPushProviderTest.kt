/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoSet(SessionScope::class)
@Inject
class CurrentPushProviderTest(
    private val pushService: PushService,
    private val sessionId: SessionId,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 110
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_current_push_provider_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_current_push_provider_description),
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val pushProvider = pushService.getCurrentPushProvider(sessionId)
        if (pushProvider == null) {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_current_push_provider_failure),
                status = NotificationTroubleshootTestState.Status.Failure()
            )
        } else if (pushProvider.supportMultipleDistributors.not()) {
            delegate.updateState(
                description = stringProvider.getString(
                    R.string.troubleshoot_notifications_test_current_push_provider_success,
                    pushProvider.name
                ),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            val distributorValue = pushProvider.getCurrentDistributorValue(sessionId)
            if (distributorValue == null) {
                // No distributors configured
                delegate.updateState(
                    description = stringProvider.getString(
                        R.string.troubleshoot_notifications_test_current_push_provider_failure_no_distributor,
                        pushProvider.name
                    ),
                    status = NotificationTroubleshootTestState.Status.Failure(false)
                )
            } else {
                val distributor = pushProvider.getDistributors().find { it.value == distributorValue }
                if (distributor == null) {
                    // Distributor has been uninstalled?
                    delegate.updateState(
                        description = stringProvider.getString(
                            R.string.troubleshoot_notifications_test_current_push_provider_failure_distributor_not_found,
                            pushProvider.name,
                            distributorValue,
                            distributorValue,
                        ),
                        status = NotificationTroubleshootTestState.Status.Failure(false)
                    )
                } else {
                    delegate.updateState(
                        description = stringProvider.getString(
                            R.string.troubleshoot_notifications_test_current_push_provider_success_with_distributor,
                            pushProvider.name,
                            distributorValue,
                        ),
                        status = NotificationTroubleshootTestState.Status.Success
                    )
                }
            }
        }
    }

    override suspend fun reset() = delegate.reset()
}
