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

package io.element.android.libraries.pushproviders.firebase.troubleshoot

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.notifications.NotificationTroubleshootTest
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestDelegate
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.core.notifications.TestFilterData
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.pushproviders.firebase.FirebaseConfig
import io.element.android.libraries.pushproviders.firebase.IsPlayServiceAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class FirebaseAvailabilityTest @Inject constructor(
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
) : NotificationTroubleshootTest {
    override val order = 300
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = "Check Firebase",
        defaultDescription = "Ensure that Firebase is available.",
        visibleWhenIdle = false,
        fakeDelay = NotificationTroubleshootTestDelegate.LONG_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override fun isRelevant(data: TestFilterData): Boolean {
        return data.currentPushProviderName == FirebaseConfig.NAME
    }

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val result = isPlayServiceAvailable.isAvailable()
        if (result) {
            delegate.updateState(
                description = "Firebase is available",
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = "Firebase is not available",
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
        }
    }

    override fun reset() = delegate.reset()
}
