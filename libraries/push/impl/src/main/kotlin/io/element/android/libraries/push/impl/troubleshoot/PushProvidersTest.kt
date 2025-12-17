/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoSet(AppScope::class)
@Inject
class PushProvidersTest(
    pushProviders: Set<@JvmSuppressWildcards PushProvider>,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    private val sortedPushProvider = pushProviders.sortedBy { it.index }
    override val order = 100
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_detect_push_provider_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_detect_push_provider_description),
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val result = sortedPushProvider.isNotEmpty()
        if (result) {
            delegate.updateState(
                description = stringProvider.getString(
                    resId = R.string.troubleshoot_notifications_test_detect_push_provider_success_2,
                    sortedPushProvider.joinToString { it.name }
                ),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_detect_push_provider_failure),
                status = NotificationTroubleshootTestState.Status.Failure()
            )
        }
    }

    override suspend fun reset() = delegate.reset()
}
