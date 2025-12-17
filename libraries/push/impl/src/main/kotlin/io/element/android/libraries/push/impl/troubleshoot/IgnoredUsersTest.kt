/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoSet(SessionScope::class)
@Inject
class IgnoredUsersTest(
    private val matrixClient: MatrixClient,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 80
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_blocked_users_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_blocked_users_description),
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val ignorerUsers = matrixClient.ignoredUsersFlow.value
        if (ignorerUsers.isEmpty()) {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_blocked_users_result_none),
                status = NotificationTroubleshootTestState.Status.Success,
            )
        } else {
            delegate.updateState(
                description = stringProvider.getQuantityString(
                    R.plurals.troubleshoot_notifications_test_blocked_users_result_some,
                    ignorerUsers.size,
                    ignorerUsers.size
                ),
                status = NotificationTroubleshootTestState.Status.Failure(
                    hasQuickFix = true,
                    isCritical = false,
                    quickFixButtonString = stringProvider.getString(R.string.troubleshoot_notifications_test_blocked_users_quick_fix),
                ),
            )
        }
    }

    override suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        navigator.navigateToBlockedUsers()
    }

    override suspend fun reset() = delegate.reset()
}
