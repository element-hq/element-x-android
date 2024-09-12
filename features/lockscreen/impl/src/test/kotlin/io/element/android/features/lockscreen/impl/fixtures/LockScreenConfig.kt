/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.fixtures

import io.element.android.features.lockscreen.impl.LockScreenConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal fun aLockScreenConfig(
    isPinMandatory: Boolean = false,
    forbiddenPinCodes: Set<String> = emptySet(),
    pinSize: Int = 4,
    maxPinCodeAttemptsBeforeLogout: Int = 3,
    gracePeriod: Duration = 3.seconds,
    isStrongBiometricsEnabled: Boolean = true,
    isWeakBiometricsEnabled: Boolean = true,
): LockScreenConfig {
    return LockScreenConfig(
        isPinMandatory = isPinMandatory,
        forbiddenPinCodes = forbiddenPinCodes,
        pinSize = pinSize,
        maxPinCodeAttemptsBeforeLogout = maxPinCodeAttemptsBeforeLogout,
        gracePeriod = gracePeriod,
        isStrongBiometricsEnabled = isStrongBiometricsEnabled,
        isWeakBiometricsEnabled = isWeakBiometricsEnabled,
    )
}
