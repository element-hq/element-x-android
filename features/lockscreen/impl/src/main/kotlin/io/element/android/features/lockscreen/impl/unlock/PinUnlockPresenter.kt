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

package io.element.android.features.lockscreen.impl.unlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlock
import io.element.android.features.lockscreen.impl.biometric.BiometricUnlockManager
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypadModel
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PinUnlockPresenter @Inject constructor(
    private val pinCodeManager: PinCodeManager,
    private val biometricUnlockManager: BiometricUnlockManager,
    private val matrixClient: MatrixClient,
    private val coroutineScope: CoroutineScope,
    private val pinUnlockHelper: PinUnlockHelper,
) : Presenter<PinUnlockState> {

    @Composable
    override fun present(): PinUnlockState {
        val pinEntryState = remember {
            mutableStateOf<AsyncData<PinEntry>>(AsyncData.Uninitialized)
        }
        val pinEntry by pinEntryState
        var remainingAttempts by remember {
            mutableStateOf<AsyncData<Int>>(AsyncData.Uninitialized)
        }
        var showWrongPinTitle by rememberSaveable {
            mutableStateOf(false)
        }
        var showSignOutPrompt by rememberSaveable {
            mutableStateOf(false)
        }
        val signOutAction = remember {
            mutableStateOf<AsyncData<String?>>(AsyncData.Uninitialized)
        }
        var biometricUnlockResult by remember {
            mutableStateOf<BiometricUnlock.AuthenticationResult?>(null)
        }
        val isUnlocked = remember {
            mutableStateOf(false)
        }
        val biometricUnlock = biometricUnlockManager.rememberBiometricUnlock()
        LaunchedEffect(Unit) {
            suspend {
                val pinCodeSize = pinCodeManager.getPinCodeSize()
                PinEntry.createEmpty(pinCodeSize)
            }.runCatchingUpdatingState(pinEntryState)
        }
        LaunchedEffect(biometricUnlock) {
            biometricUnlock.setup()
            biometricUnlock.authenticate()
        }

        LaunchedEffect(pinEntry) {
            if (pinEntry.isComplete()) {
                val isVerified = pinCodeManager.verifyPinCode(pinEntry.toText())
                if (!isVerified) {
                    pinEntryState.value = pinEntry.clear()
                    showWrongPinTitle = true
                }
            }
            val remainingAttemptsNumber = pinCodeManager.getRemainingPinCodeAttemptsNumber()
            remainingAttempts = AsyncData.Success(remainingAttemptsNumber)
            if (remainingAttemptsNumber == 0) {
                showSignOutPrompt = true
            }
        }
        pinUnlockHelper.OnUnlockEffect {
            isUnlocked.value = true
        }

        fun handleEvents(event: PinUnlockEvents) {
            when (event) {
                is PinUnlockEvents.OnPinKeypadPressed -> {
                    pinEntryState.value = pinEntry.process(event.pinKeypadModel)
                }
                PinUnlockEvents.OnForgetPin -> showSignOutPrompt = true
                PinUnlockEvents.ClearSignOutPrompt -> showSignOutPrompt = false
                PinUnlockEvents.SignOut -> {
                    if (showSignOutPrompt) {
                        showSignOutPrompt = false
                        coroutineScope.signOut(signOutAction)
                    }
                }
                PinUnlockEvents.OnUseBiometric -> {
                    coroutineScope.launch {
                        biometricUnlockResult = biometricUnlock.authenticate()
                    }
                }
                PinUnlockEvents.ClearBiometricError -> {
                    biometricUnlockResult = null
                }
                is PinUnlockEvents.OnPinEntryChanged -> {
                    pinEntryState.value = pinEntry.process(event.entryAsText)
                }
            }
        }
        return PinUnlockState(
            pinEntry = pinEntry,
            showWrongPinTitle = showWrongPinTitle,
            remainingAttempts = remainingAttempts,
            showSignOutPrompt = showSignOutPrompt,
            signOutAction = signOutAction.value,
            showBiometricUnlock = biometricUnlock.isActive,
            biometricUnlockResult = biometricUnlockResult,
            isUnlocked = isUnlocked.value,
            eventSink = ::handleEvents
        )
    }

    private fun AsyncData<PinEntry>.isComplete(): Boolean {
        return dataOrNull()?.isComplete().orFalse()
    }

    private fun AsyncData<PinEntry>.toText(): String {
        return dataOrNull()?.toText() ?: ""
    }

    private fun AsyncData<PinEntry>.clear(): AsyncData<PinEntry> {
        return when (this) {
            is AsyncData.Success -> AsyncData.Success(data.clear())
            else -> this
        }
    }

    private fun AsyncData<PinEntry>.process(pinKeypadModel: PinKeypadModel): AsyncData<PinEntry> {
        return when (this) {
            is AsyncData.Success -> {
                val pinEntry = when (pinKeypadModel) {
                    PinKeypadModel.Back -> data.deleteLast()
                    is PinKeypadModel.Number -> data.addDigit(pinKeypadModel.number)
                    PinKeypadModel.Empty -> data
                }
                AsyncData.Success(pinEntry)
            }
            else -> this
        }
    }

    private fun AsyncData<PinEntry>.process(pinEntryAsText: String): AsyncData<PinEntry> {
        return when (this) {
            is AsyncData.Success -> {
                val pinEntry = data.fillWith(pinEntryAsText)
                AsyncData.Success(pinEntry)
            }
            else -> this
        }
    }

    private fun CoroutineScope.signOut(signOutAction: MutableState<AsyncData<String?>>) = launch {
        suspend {
            matrixClient.logout(ignoreSdkError = true)
        }.runCatchingUpdatingState(signOutAction)
    }
}
