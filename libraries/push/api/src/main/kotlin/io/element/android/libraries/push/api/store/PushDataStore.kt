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

package io.element.android.libraries.push.api.store

import io.element.android.libraries.push.api.model.BackgroundSyncMode
import kotlinx.coroutines.flow.Flow

interface PushDataStore {
    val pushCounterFlow: Flow<Int>

    fun areNotificationEnabledForDevice(): Boolean
    fun setNotificationEnabledForDevice(enabled: Boolean)

    fun backgroundSyncTimeOut(): Int
    fun setBackgroundSyncTimeout(timeInSecond: Int)
    fun backgroundSyncDelay(): Int
    fun setBackgroundSyncDelay(timeInSecond: Int)
    fun isBackgroundSyncEnabled(): Boolean
    fun setFdroidSyncBackgroundMode(mode: BackgroundSyncMode)
    fun getFdroidSyncBackgroundMode(): BackgroundSyncMode

    /**
     * Return true if Pin code is disabled, or if user set the settings to see full notification content.
     */
    fun useCompleteNotificationFormat(): Boolean
}
