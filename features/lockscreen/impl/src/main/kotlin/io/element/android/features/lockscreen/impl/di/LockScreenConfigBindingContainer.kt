/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.features.lockscreen.impl.LockScreenConfig

@ContributesTo(AppScope::class)
@BindingContainer
object LockScreenConfigBindingContainer {
    @Provides
    fun providesLockScreenConfig(): LockScreenConfig = LockScreenConfig(
        isPinMandatory = io.element.android.appconfig.LockScreenConfig.IS_PIN_MANDATORY,
        forbiddenPinCodes = io.element.android.appconfig.LockScreenConfig.FORBIDDEN_PIN_CODES,
        pinSize = io.element.android.appconfig.LockScreenConfig.PIN_SIZE,
        maxPinCodeAttemptsBeforeLogout = io.element.android.appconfig.LockScreenConfig.MAX_PIN_CODE_ATTEMPTS_BEFORE_LOGOUT,
        gracePeriod = io.element.android.appconfig.LockScreenConfig.GRACE_PERIOD,
        isStrongBiometricsEnabled = io.element.android.appconfig.LockScreenConfig.IS_STRONG_BIOMETRICS_ENABLED,
        isWeakBiometricsEnabled = io.element.android.appconfig.LockScreenConfig.IS_WEAK_BIOMETRICS_ENABLED,
    )
}
