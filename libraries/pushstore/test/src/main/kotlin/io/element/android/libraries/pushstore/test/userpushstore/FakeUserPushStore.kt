/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.pushstore.test.userpushstore

import io.element.android.libraries.pushstore.api.UserPushStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPushStore(
    private var pushProviderName: String? = null
) : UserPushStore {
    private var currentRegisteredPushKey: String? = null
    private val notificationEnabledForDevice = MutableStateFlow(true)
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

    override suspend fun reset() {
    }
}
