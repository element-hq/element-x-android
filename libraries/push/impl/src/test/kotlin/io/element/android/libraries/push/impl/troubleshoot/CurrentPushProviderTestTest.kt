/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.troubleshoot

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.push.test.FakeGetCurrentPushProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CurrentPushProviderTestTest {
    @Test
    fun `test CurrentPushProviderTest with a push provider`() = runTest {
        val sut = CurrentPushProviderTest(
            getCurrentPushProvider = FakeGetCurrentPushProvider("foo"),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
            assertThat(lastItem.description).contains("foo")
        }
    }

    @Test
    fun `test CurrentPushProviderTest without push provider`() = runTest {
        val sut = CurrentPushProviderTest(
            getCurrentPushProvider = FakeGetCurrentPushProvider(null),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
        }
    }
}
