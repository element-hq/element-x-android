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

package io.element.android.libraries.pushstore.impl

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserPushStoreDataStoreTest {
    @Test
    fun `test getPushProviderName`() = runTest {
        val sut = createUserPushStoreDataStore()
        assertThat(sut.getPushProviderName()).isNull()
        sut.setPushProviderName("name")
        assertThat(sut.getPushProviderName()).isEqualTo("name")
    }

    @Test
    fun `test getCurrentRegisteredPushKey`() = runTest {
        val sut = createUserPushStoreDataStore()
        assertThat(sut.getCurrentRegisteredPushKey()).isNull()
        sut.setCurrentRegisteredPushKey("aKey")
        assertThat(sut.getCurrentRegisteredPushKey()).isEqualTo("aKey")
        sut.setCurrentRegisteredPushKey(null)
        assertThat(sut.getCurrentRegisteredPushKey()).isNull()
    }

    @Test
    fun `test getNotificationEnabledForDevice`() = runTest {
        val sut = createUserPushStoreDataStore()
        assertThat(sut.getNotificationEnabledForDevice().first()).isTrue()
        sut.setNotificationEnabledForDevice(false)
        assertThat(sut.getNotificationEnabledForDevice().first()).isFalse()
        sut.setNotificationEnabledForDevice(true)
        assertThat(sut.getNotificationEnabledForDevice().first()).isTrue()
    }

    @Test
    fun `test useCompleteNotificationFormat`() = runTest {
        val sut = createUserPushStoreDataStore()
        assertThat(sut.useCompleteNotificationFormat()).isTrue()
    }

    @Test
    fun `test ignoreRegistrationError`() = runTest {
        val sut = createUserPushStoreDataStore()
        assertThat(sut.ignoreRegistrationError().first()).isFalse()
        sut.setIgnoreRegistrationError(true)
        assertThat(sut.ignoreRegistrationError().first()).isTrue()
        sut.setIgnoreRegistrationError(false)
        assertThat(sut.ignoreRegistrationError().first()).isFalse()
    }

    @Test
    fun `test reset`() = runTest {
        val sut = createUserPushStoreDataStore()
        sut.setPushProviderName("name")
        sut.setCurrentRegisteredPushKey("aKey")
        sut.setNotificationEnabledForDevice(false)
        sut.setIgnoreRegistrationError(true)
        sut.reset()
        assertThat(sut.getPushProviderName()).isNull()
        assertThat(sut.getCurrentRegisteredPushKey()).isNull()
        assertThat(sut.getNotificationEnabledForDevice().first()).isTrue()
        assertThat(sut.ignoreRegistrationError().first()).isFalse()
    }

    @Test
    fun `ensure a store is created per session`() = runTest {
        val sut1 = createUserPushStoreDataStore()
        sut1.setPushProviderName("name")
        val sut2 = createUserPushStoreDataStore(A_SESSION_ID_2)
        assertThat(sut1.getPushProviderName()).isEqualTo("name")
        assertThat(sut2.getPushProviderName()).isNull()
    }

    private fun createUserPushStoreDataStore(
        sessionId: SessionId = A_SESSION_ID,
    ) = UserPushStoreDataStore(
        context = InstrumentationRegistry.getInstrumentation().context,
        userId = sessionId,
    )
}
