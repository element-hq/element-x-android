/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.test.userpushstore

import io.element.android.libraries.pushstore.api.UserPushStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPushStore(
    private var pushProviderName: String? = null
) : UserPushStore {
    private var currentRegisteredPushKey: String? = null
    private val notificationEnabledForDevice = MutableStateFlow(true)
    private val ignoreRegistrationError = MutableStateFlow(false)
    override suspend fun getPushProviderName(): String? {
        return pushProviderName
    }

    override suspend fun setPushProviderName(value: String) {
        pushProviderName = value
    }

    override suspend fun getCurrentRegisteredPushKey(): String? {
        return currentRegisteredPushKey
    }

    override suspend fun setCurrentRegisteredPushKey(value: String?) {
        currentRegisteredPushKey = value
    }

    override fun getNotificationEnabledForDevice(): Flow<Boolean> {
        return notificationEnabledForDevice
    }

    override suspend fun setNotificationEnabledForDevice(enabled: Boolean) {
        notificationEnabledForDevice.value = enabled
    }

    override fun useCompleteNotificationFormat(): Boolean {
        return true
    }

    override fun ignoreRegistrationError(): Flow<Boolean> {
        return ignoreRegistrationError
    }

    override suspend fun setIgnoreRegistrationError(ignore: Boolean) {
        ignoreRegistrationError.value = ignore
    }

    override suspend fun reset() {
        pushProviderName = null
    }
}
