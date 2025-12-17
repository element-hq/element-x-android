/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushApiFactory
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushSessionPushConfigProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@ContributesIntoSet(SessionScope::class)
@Inject
class UnifiedPushMatrixGatewayTest(
    private val sessionId: SessionId,
    private val unifiedPushApiFactory: UnifiedPushApiFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val unifiedPushSessionPushConfigProvider: UnifiedPushSessionPushConfigProvider,
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
        val config = unifiedPushSessionPushConfigProvider.provide(sessionId)
        if (config == null) {
            delegate.updateState(
                description = "No current push provider",
                status = NotificationTroubleshootTestState.Status.Failure()
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
                            status = NotificationTroubleshootTestState.Status.Failure()
                        )
                    }
                } catch (throwable: Throwable) {
                    delegate.updateState(
                        description = "Fail to check the gateway ${config.url}: ${throwable.localizedMessage}",
                        status = NotificationTroubleshootTestState.Status.Failure()
                    )
                }
            }
        }
    }

    override suspend fun reset() = delegate.reset()
}
