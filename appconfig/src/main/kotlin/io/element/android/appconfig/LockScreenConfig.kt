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

package io.element.android.appconfig

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.libraries.di.AppScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for the lock screen feature.
 * @property isPinMandatory Whether the PIN is mandatory or not.
 * @property pinBlacklist Some PINs are forbidden.
 * @property pinSize The size of the PIN.
 * @property maxPinCodeAttemptsBeforeLogout Number of attempts before the user is logged out.
 * @property gracePeriod Time period before locking the app once backgrounded.
 * @property isStrongBiometricsEnabled Authentication with strong methods (fingerprint, some face/iris unlock implementations) is supported.
 * @property isWeakBiometricsEnabled Authentication with weak methods (most face/iris unlock implementations) is supported.
 */
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
        isPinMandatory = false,
        pinBlacklist = setOf("0000", "1234"),
        pinSize = 4,
        maxPinCodeAttemptsBeforeLogout = 3,
        gracePeriod = 0.seconds,
        isStrongBiometricsEnabled = true,
        isWeakBiometricsEnabled = true,
    )
}
