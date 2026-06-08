/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.lockscreen.api.DeviceUnlockPrompt
import io.element.android.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import io.element.android.features.lockscreen.impl.unlock.PinUnlockHelper

@ContributesBinding(AppScope::class)
class DefaultDeviceUnlockPrompt(
    private val pinUnlockHelper: PinUnlockHelper,
    private val biometricAuthenticatorManager: BiometricAuthenticatorManager,
) : DeviceUnlockPrompt {
    @Composable
    override fun OnUnlockEffect(onUnlockResult: (Boolean) -> Unit) {
        pinUnlockHelper.OnUnlockEffect { onUnlockResult(it) }
    }

    @Composable
    override fun ShowPrompt() {
        val biometricUnlock = biometricAuthenticatorManager.rememberUnlockBiometricAuthenticator(forFeatureUnlock = true)
        LaunchedEffect(biometricUnlock) {
            biometricUnlock.setup()
            biometricUnlock.authenticate()
        }
    }
}
