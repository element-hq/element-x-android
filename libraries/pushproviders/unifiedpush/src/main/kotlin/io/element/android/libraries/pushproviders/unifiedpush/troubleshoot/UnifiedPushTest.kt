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

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.notifications.NotificationTroubleshootTest
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestDelegate
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.core.notifications.TestFilterData
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushDistributorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class UnifiedPushTest @Inject constructor(
    private val unifiedPushDistributorProvider: UnifiedPushDistributorProvider,
    private val openDistributorWebPageAction: OpenDistributorWebPageAction,
) : NotificationTroubleshootTest {
    override val order = 400
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = "Check UnifiedPush",
        defaultDescription = "Ensure that UnifiedPush distributors are available.",
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
                description = "Distributors found: ${distributors.joinToString { it.name }}",
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = "No push distributors found",
                status = NotificationTroubleshootTestState.Status.Failure(true)
            )
        }
    }

    override fun reset() = delegate.reset()

    override suspend fun quickFix(coroutineScope: CoroutineScope) {
        openDistributorWebPageAction.execute()
    }
}
