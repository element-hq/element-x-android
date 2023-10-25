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
import io.element.android.appconfig.LockScreenConfig
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.unlock.keypad.PinKeypadModel
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PinUnlockPresenter @Inject constructor(
    private val pinCodeManager: PinCodeManager,
    private val matrixClient: MatrixClient,
    private val coroutineScope: CoroutineScope,
) : Presenter<PinUnlockState> {

    @Composable
    override fun present(): PinUnlockState {
        var pinEntry by remember {
            //TODO fetch size from db
            mutableStateOf(PinEntry.createEmpty(LockScreenConfig.PIN_SIZE))
        }
        var remainingAttempts by remember {
            mutableStateOf<Async<Int>>(Async.Uninitialized)
        }
        var showWrongPinTitle by rememberSaveable {
            mutableStateOf(false)
        }
        var showSignOutPrompt by rememberSaveable {
            mutableStateOf(false)
        }

        val signOutAction = remember {
            mutableStateOf<Async<String?>>(Async.Uninitialized)
        }

        LaunchedEffect(pinEntry) {
            if (pinEntry.isComplete()) {
                val isVerified = pinCodeManager.verifyPinCode(pinEntry.toText())
                if (!isVerified) {
                    pinEntry = pinEntry.clear()
                    showWrongPinTitle = true
                }
            }
            val remainingAttemptsNumber = pinCodeManager.getRemainingPinCodeAttemptsNumber()
            remainingAttempts = Async.Success(remainingAttemptsNumber)
            if (remainingAttemptsNumber == 0) {
                showSignOutPrompt = true
            }
        }

        fun handleEvents(event: PinUnlockEvents) {
            when (event) {
                is PinUnlockEvents.OnPinKeypadPressed -> {
                    pinEntry = pinEntry.process(event.pinKeypadModel)
                }
                PinUnlockEvents.OnForgetPin -> showSignOutPrompt = true
                PinUnlockEvents.ClearSignOutPrompt -> showSignOutPrompt = false
                PinUnlockEvents.SignOut -> {
                    showSignOutPrompt = false
                    coroutineScope.signOut(signOutAction)
                }
                PinUnlockEvents.OnUseBiometric -> {
                    //TODO
                }
            }
        }
        return PinUnlockState(
            pinEntry = pinEntry,
            showWrongPinTitle = showWrongPinTitle,
            remainingAttempts = remainingAttempts,
            showSignOutPrompt = showSignOutPrompt,
            signOutAction = signOutAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.signOut(signOutAction: MutableState<Async<String?>>) = launch {
        suspend {
            matrixClient.logout()
        }.runCatchingUpdatingState(signOutAction)
    }

    private fun PinEntry.process(pinKeypadModel: PinKeypadModel): PinEntry {
        return when (pinKeypadModel) {
            PinKeypadModel.Back -> deleteLast()
            is PinKeypadModel.Number -> addDigit(pinKeypadModel.number)
            PinKeypadModel.Empty -> this
        }
    }
}
