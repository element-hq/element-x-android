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
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class PushProvidersTest @Inject constructor(
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
                description = stringProvider.getQuantityString(
                    resId = R.plurals.troubleshoot_notifications_test_detect_push_provider_success,
                    quantity = sortedPushProvider.size,
                    sortedPushProvider.size,
                    sortedPushProvider.joinToString { it.name }
                ),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_detect_push_provider_failure),
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
        }
    }

    override suspend fun reset() = delegate.reset()
}
