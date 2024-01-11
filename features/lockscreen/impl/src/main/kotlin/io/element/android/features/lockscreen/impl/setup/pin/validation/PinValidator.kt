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

package io.element.android.features.lockscreen.impl.setup.pin.validation

import io.element.android.appconfig.LockScreenConfig
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import javax.inject.Inject

class PinValidator @Inject constructor(private val lockScreenConfig: LockScreenConfig) {
    sealed interface Result {
        data object Valid : Result
        data class Invalid(val failure: SetupPinFailure) : Result
    }

    fun isPinValid(pinEntry: PinEntry): Result {
        val pinAsText = pinEntry.toText()
        val isBlacklisted = lockScreenConfig.pinBlacklist.any { it == pinAsText }
        return if (isBlacklisted) {
            Result.Invalid(SetupPinFailure.PinBlacklisted)
        } else {
            Result.Valid
        }
    }
}
