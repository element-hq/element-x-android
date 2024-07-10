/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    val pinBlacklist: Set<String>,
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
        pinBlacklist = AppConfigLockScreenConfig.PIN_BLACKLIST,
        pinSize = AppConfigLockScreenConfig.PIN_SIZE,
        maxPinCodeAttemptsBeforeLogout = AppConfigLockScreenConfig.MAX_PIN_CODE_ATTEMPTS_BEFORE_LOGOUT,
        gracePeriod = AppConfigLockScreenConfig.GRACE_PERIOD,
        isStrongBiometricsEnabled = AppConfigLockScreenConfig.IS_STRONG_BIOMETRICS_ENABLED,
        isWeakBiometricsEnabled = AppConfigLockScreenConfig.IS_WEAK_BIOMETRICS_ENABLED,
    )
}
