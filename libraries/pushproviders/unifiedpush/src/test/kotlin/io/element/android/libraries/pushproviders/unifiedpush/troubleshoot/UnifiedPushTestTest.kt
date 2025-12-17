/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.libraries.troubleshoot.test.FakeNotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.test.runAndTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UnifiedPushTestTest {
    @Test
    fun `test UnifiedPushTest success`() = runTest {
        val sut = UnifiedPushTest(
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(
                getDistributorsResult = listOf(
                    Distributor("value", "Name"),
                )
            ),
            openDistributorWebPageAction = FakeOpenDistributorWebPageAction(),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test UnifiedPushTest error`() = runTest {
        val providers = FakeUnifiedPushDistributorProvider()
        val sut = UnifiedPushTest(
            unifiedPushDistributorProvider = providers,
            openDistributorWebPageAction = FakeOpenDistributorWebPageAction(
                executeAction = {
                    providers.setDistributorsResult(
                        listOf(
                            Distributor("value", "Name"),
                        )
                    )
                }
            ),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true))
            // Quick fix
            backgroundScope.launch {
                sut.quickFix(this, FakeNotificationTroubleshootNavigator())
                sut.run(this)
            }
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test UnifiedPushTest error and reset`() = runTest {
        val providers = FakeUnifiedPushDistributorProvider()
        val sut = UnifiedPushTest(
            unifiedPushDistributorProvider = providers,
            openDistributorWebPageAction = FakeOpenDistributorWebPageAction(
                executeAction = {
                    providers.setDistributorsResult(
                        listOf(
                            Distributor("value", "Name"),
                        )
                    )
                }
            ),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true))
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
        }
    }

    @Test
    fun `test isRelevant`() {
        val sut = UnifiedPushTest(
            unifiedPushDistributorProvider = FakeUnifiedPushDistributorProvider(),
            openDistributorWebPageAction = FakeOpenDistributorWebPageAction(),
            stringProvider = FakeStringProvider(),
        )
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = UnifiedPushConfig.NAME))).isTrue()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "other"))).isFalse()
    }
}
