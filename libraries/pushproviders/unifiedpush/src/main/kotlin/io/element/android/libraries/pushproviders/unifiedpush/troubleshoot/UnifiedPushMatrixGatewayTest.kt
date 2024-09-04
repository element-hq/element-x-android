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
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushApiFactory
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class UnifiedPushMatrixGatewayTest @Inject constructor(
    private val unifiedPushApiFactory: UnifiedPushApiFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val pushProvider: PushProvider,
) : NotificationTroubleshootTest {
    override val order = 450
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = "Test push gateway",
        defaultDescription = "Ensure that the push gateway is valid.",
        visibleWhenIdle = false,
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override fun isRelevant(data: TestFilterData): Boolean {
        return data.currentPushProviderName == UnifiedPushConfig.NAME
    }

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val config = pushProvider.getCurrentUserPushConfig()
        if (config == null) {
            delegate.updateState(
                description = "No current push provider",
                status = NotificationTroubleshootTestState.Status.Failure(false)
            )
        } else {
            val gatewayBaseUrl = config.url.removeSuffix("/_matrix/push/v1/notify")
            // Checking if the gateway is a Matrix gateway
            coroutineScope.launch(coroutineDispatchers.io) {
                val api = unifiedPushApiFactory.create(gatewayBaseUrl)
                try {
                    val discoveryResponse = api.discover()
                    if (discoveryResponse.unifiedpush.gateway == "matrix") {
                        delegate.updateState(
                            description = "${config.url} is a Matrix gateway.",
                            status = NotificationTroubleshootTestState.Status.Success
                        )
                    } else {
                        delegate.updateState(
                            description = "${config.url} is not a Matrix gateway.",
                            status = NotificationTroubleshootTestState.Status.Failure(false)
                        )
                    }
                } catch (throwable: Throwable) {
                    delegate.updateState(
                        description = "Fail to check the gateway ${config.url}: ${throwable.localizedMessage}",
                        status = NotificationTroubleshootTestState.Status.Failure(false)
                    )
                }
            }
        }
    }

    override suspend fun reset() = delegate.reset()
}
