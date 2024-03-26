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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.pushproviders.firebase.FakeFirebaseTroubleshooter
import io.element.android.libraries.pushproviders.firebase.InMemoryFirebaseStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FirebaseTokenTestTest {
    @Test
    fun `test FirebaseTokenTest success`() = runTest {
        val sut = FirebaseTokenTest(
            firebaseStore = InMemoryFirebaseStore(FAKE_TOKEN),
            firebaseTroubleshooter = FakeFirebaseTroubleshooter(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
            assertThat(lastItem.description).contains(FAKE_TOKEN.take(8))
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
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(true))
            // Quick fix
            sut.quickFix(this)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    companion object {
        private const val FAKE_TOKEN = "abcdefghijk"
    }
}
