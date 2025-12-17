/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase.troubleshoot

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.pushproviders.firebase.FakeIsPlayServiceAvailable
import io.element.android.libraries.pushproviders.firebase.FirebaseConfig
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.libraries.troubleshoot.test.runAndTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FirebaseAvailabilityTestTest {
    @Test
    fun `test FirebaseAvailabilityTest success`() = runTest {
        val sut = FirebaseAvailabilityTest(
            isPlayServiceAvailable = FakeIsPlayServiceAvailable(true),
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
    fun `test FirebaseAvailabilityTest failure`() = runTest {
        val sut = FirebaseAvailabilityTest(
            isPlayServiceAvailable = FakeIsPlayServiceAvailable(false),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure())
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
        }
    }

    @Test
    fun `test FirebaseAvailabilityTest isRelevant`() {
        val sut = FirebaseAvailabilityTest(
            isPlayServiceAvailable = FakeIsPlayServiceAvailable(false),
            stringProvider = FakeStringProvider(),
        )
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "unknown"))).isFalse()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = FirebaseConfig.NAME))).isTrue()
    }
}
