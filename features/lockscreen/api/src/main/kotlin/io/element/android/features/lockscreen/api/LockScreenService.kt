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

package io.element.android.features.lockscreen.api

import kotlinx.coroutines.flow.StateFlow

interface LockScreenService {
    /**
     * The current lock state of the app.
     */
    val lockState: StateFlow<LockScreenLockState>

    /**
     * Check if setting up the lock screen is required.
     * @return true if the lock screen is mandatory and not setup yet, false otherwise.
     */
    suspend fun isSetupRequired(): Boolean
}
