/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class CurrentPushProviderTest @Inject constructor(
    private val getCurrentPushProvider: GetCurrentPushProvider,
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
        val provider = getCurrentPushProvider.getCurrentPushProvider()
        if (provider != null) {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_current_push_provider_success, provider),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_current_push_provider_failure),
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
        }
    }

    override suspend fun reset() = delegate.reset()
}
