/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.pushproviders.unifiedpush.R
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushDistributorProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoSet(AppScope::class)
@Inject
class UnifiedPushTest(
    private val unifiedPushDistributorProvider: UnifiedPushDistributorProvider,
    private val openDistributorWebPageAction: OpenDistributorWebPageAction,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 400
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_unified_push_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_unified_push_description),
        visibleWhenIdle = false,
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override fun isRelevant(data: TestFilterData): Boolean {
        return data.currentPushProviderName == UnifiedPushConfig.NAME
    }

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val distributors = unifiedPushDistributorProvider.getDistributors()
        if (distributors.isNotEmpty()) {
            delegate.updateState(
                description = stringProvider.getQuantityString(
                    resId = R.plurals.troubleshoot_notifications_test_unified_push_success,
                    quantity = distributors.size,
                    distributors.size,
                    distributors.joinToString { it.name }
                ),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_unified_push_failure),
                status = NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true)
            )
        }
    }

    override suspend fun reset() = delegate.reset()

    override suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        openDistributorWebPageAction.execute()
    }
}
