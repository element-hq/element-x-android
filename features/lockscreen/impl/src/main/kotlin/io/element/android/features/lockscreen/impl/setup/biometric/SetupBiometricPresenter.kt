/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetupBiometricPresenter @Inject constructor(
    private val lockScreenStore: LockScreenStore,
) : Presenter<SetupBiometricState> {
    @Composable
    override fun present(): SetupBiometricState {
        var isBiometricSetupDone by remember {
            mutableStateOf(false)
        }

        val coroutineScope = rememberCoroutineScope()

        fun handleEvents(event: SetupBiometricEvents) {
            when (event) {
                SetupBiometricEvents.AllowBiometric -> coroutineScope.launch {
                    lockScreenStore.setIsBiometricUnlockAllowed(true)
                    isBiometricSetupDone = true
                }
                SetupBiometricEvents.UsePin -> coroutineScope.launch {
                    lockScreenStore.setIsBiometricUnlockAllowed(false)
                    isBiometricSetupDone = true
                }
            }
        }

        return SetupBiometricState(
            isBiometricSetupDone = isBiometricSetupDone,
            eventSink = ::handleEvents
        )
    }
}
