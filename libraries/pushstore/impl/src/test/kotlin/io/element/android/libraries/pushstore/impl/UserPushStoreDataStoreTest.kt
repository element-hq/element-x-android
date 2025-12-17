/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
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
        factory = FakePreferenceDataStoreFactory(),
    )
}
