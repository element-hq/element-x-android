/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.pin.validation

import io.element.android.features.lockscreen.impl.LockScreenConfig
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import javax.inject.Inject

class PinValidator @Inject constructor(private val lockScreenConfig: LockScreenConfig) {
    sealed interface Result {
        data object Valid : Result
        data class Invalid(val failure: SetupPinFailure) : Result
    }

    fun isPinValid(pinEntry: PinEntry): Result {
        val pinAsText = pinEntry.toText()
        val isForbidden = lockScreenConfig.forbiddenPinCodes.any { it == pinAsText }
        return if (isForbidden) {
            Result.Invalid(SetupPinFailure.ForbiddenPin)
        } else {
            Result.Valid
        }
    }
}
