/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.AppScope
import kotlin.time.Duration
import io.element.android.appconfig.LockScreenConfig as AppConfigLockScreenConfig

data class LockScreenConfig(
    val isPinMandatory: Boolean,
    val forbiddenPinCodes: Set<String>,
    val pinSize: Int,
    val maxPinCodeAttemptsBeforeLogout: Int,
    val gracePeriod: Duration,
    val isStrongBiometricsEnabled: Boolean,
    val isWeakBiometricsEnabled: Boolean,
)

@ContributesTo(AppScope::class)
@Module
object LockScreenConfigModule {
    @Provides
    fun providesLockScreenConfig(): LockScreenConfig = LockScreenConfig(
        isPinMandatory = AppConfigLockScreenConfig.IS_PIN_MANDATORY,
        forbiddenPinCodes = AppConfigLockScreenConfig.FORBIDDEN_PIN_CODES,
        pinSize = AppConfigLockScreenConfig.PIN_SIZE,
        maxPinCodeAttemptsBeforeLogout = AppConfigLockScreenConfig.MAX_PIN_CODE_ATTEMPTS_BEFORE_LOGOUT,
        gracePeriod = AppConfigLockScreenConfig.GRACE_PERIOD,
        isStrongBiometricsEnabled = AppConfigLockScreenConfig.IS_STRONG_BIOMETRICS_ENABLED,
        isWeakBiometricsEnabled = AppConfigLockScreenConfig.IS_WEAK_BIOMETRICS_ENABLED,
    )
}
