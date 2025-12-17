/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.test.runAndTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CurrentPushProviderTestTest {
    @Test
    fun `test CurrentPushProviderTest with a push provider and a distributor`() = runTest {
        val sut = CurrentPushProviderTest(
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        name = "foo",
                        currentDistributorValue = { "aDistributor" },
                    )
                }
            ),
            stringProvider = FakeStringProvider(),
            sessionId = A_SESSION_ID,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
            assertThat(lastItem.description).contains("foo")
        }
    }

    @Test
    fun `test CurrentPushProviderTest with a push provider supporting multiple distributors, distributor found`() = runTest {
        val sut = CurrentPushProviderTest(
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        name = "foo",
                        currentDistributorValue = { "aDistributor" },
                        supportMultipleDistributors = true,
                        distributors = listOf(Distributor("aDistributor", "aDistributor"))
                    )
                },
            ),
            stringProvider = FakeStringProvider(),
            sessionId = A_SESSION_ID,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
            assertThat(lastItem.description).contains("foo")
        }
    }

    @Test
    fun `test CurrentPushProviderTest with a push provider supporting multiple distributors, no distributor`() = runTest {
        val sut = CurrentPushProviderTest(
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        name = "foo",
                        currentDistributorValue = { null },
                        supportMultipleDistributors = true,
                    )
                },
            ),
            stringProvider = FakeStringProvider(),
            sessionId = A_SESSION_ID,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure())
        }
    }

    @Test
    fun `test CurrentPushProviderTest with a push provider supporting multiple distributors, distributor not found`() = runTest {
        val sut = CurrentPushProviderTest(
            pushService = FakePushService(
                currentPushProvider = {
                    FakePushProvider(
                        name = "foo",
                        currentDistributorValue = { "aDistributor" },
                        supportMultipleDistributors = true,
                        distributors = emptyList()
                    )
                },
            ),
            stringProvider = FakeStringProvider(),
            sessionId = A_SESSION_ID,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure())
        }
    }

    @Test
    fun `test CurrentPushProviderTest without push provider`() = runTest {
        val sut = CurrentPushProviderTest(
            pushService = FakePushService(
                currentPushProvider = { null },
            ),
            stringProvider = FakeStringProvider(),
            sessionId = A_SESSION_ID,
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure())
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
        }
    }
}
