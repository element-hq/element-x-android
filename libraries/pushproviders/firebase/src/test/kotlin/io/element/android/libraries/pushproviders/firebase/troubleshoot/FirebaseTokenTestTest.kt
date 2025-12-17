/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase.troubleshoot

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.pushproviders.firebase.FakeFirebaseTroubleshooter
import io.element.android.libraries.pushproviders.firebase.FirebaseConfig
import io.element.android.libraries.pushproviders.firebase.InMemoryFirebaseStore
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.libraries.troubleshoot.test.FakeNotificationTroubleshootNavigator
import io.element.android.libraries.troubleshoot.test.runAndTestState
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FirebaseTokenTestTest {
    @Test
    fun `test FirebaseTokenTest success`() = runTest {
        val sut = FirebaseTokenTest(
            firebaseStore = InMemoryFirebaseStore(FAKE_TOKEN),
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
            assertThat(lastItem.description).contains(FAKE_TOKEN.takeLast(8))
            assertThat(lastItem.description).doesNotContain(FAKE_TOKEN)
        }
    }

    @Test
    fun `test FirebaseTokenTest error`() = runTest {
        val firebaseStore = InMemoryFirebaseStore(null)
        val sut = FirebaseTokenTest(
            firebaseStore = firebaseStore,
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(
                troubleShootResult = {
                    firebaseStore.storeFcmToken(FAKE_TOKEN)
                    Result.success(Unit)
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
            sut.quickFix(this, FakeNotificationTroubleshootNavigator())
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test FirebaseTokenTest error and reset`() = runTest {
        val firebaseStore = InMemoryFirebaseStore(null)
        val sut = FirebaseTokenTest(
            firebaseStore = firebaseStore,
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(
                troubleShootResult = {
                    firebaseStore.storeFcmToken(FAKE_TOKEN)
                    Result.success(Unit)
                }
            ),
            stringProvider = FakeStringProvider(),
        )
        sut.runAndTestState {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(hasQuickFix = true))
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
        }
    }

    @Test
    fun `test FirebaseTokenTest isRelevant`() {
        val sut = FirebaseTokenTest(
            firebaseStore = InMemoryFirebaseStore(null),
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(),
            stringProvider = FakeStringProvider(),
        )
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "unknown"))).isFalse()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = FirebaseConfig.NAME))).isTrue()
    }

    companion object {
        private const val FAKE_TOKEN = "abcdefghijk"
    }
}
