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

package io.element.android.libraries.pushstore.api
import kotlinx.coroutines.flow.Flow

/**
 * Store data related to push about a user.
 */
interface UserPushStore {
    suspend fun getPushProviderName(): String?
    suspend fun setPushProviderName(value: String)
    suspend fun getCurrentRegisteredPushKey(): String?
    suspend fun setCurrentRegisteredPushKey(value: String)

    fun getNotificationEnabledForDevice(): Flow<Boolean>
    suspend fun setNotificationEnabledForDevice(enabled: Boolean)

    /**
     * Return true if Pin code is disabled, or if user set the settings to see full notification content.
     */
    fun useCompleteNotificationFormat(): Boolean

    suspend fun reset()
}
