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

package io.element.android.features.lockscreen.impl.fixtures

import io.element.android.features.lockscreen.impl.LockScreenConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun aLockScreenConfig(
    isPinMandatory: Boolean = false,
    pinBlacklist: Set<String> = emptySet(),
    pinSize: Int = 4,
    maxPinCodeAttemptsBeforeLogout: Int = 3,
    gracePeriod: Duration = 3.seconds,
    isStrongBiometricsEnabled: Boolean = true,
    isWeakBiometricsEnabled: Boolean = true,
): LockScreenConfig {
    return LockScreenConfig(
        isPinMandatory = isPinMandatory,
        pinBlacklist = pinBlacklist,
        pinSize = pinSize,
        maxPinCodeAttemptsBeforeLogout = maxPinCodeAttemptsBeforeLogout,
        gracePeriod = gracePeriod,
        isStrongBiometricsEnabled = isStrongBiometricsEnabled,
        isWeakBiometricsEnabled = isWeakBiometricsEnabled,
    )
}
