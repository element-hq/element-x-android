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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.lockscreen.api.LockScreenStateService
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.unlock.numpad.PinKeypadModel
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PinUnlockPresenter @Inject constructor(
    private val pinStateService: LockScreenStateService,
    private val coroutineScope: CoroutineScope,
) : Presenter<PinUnlockState> {

    @Composable
    override fun present(): PinUnlockState {
        var pinEntry by remember {
            mutableStateOf(PinEntry.empty(4))
        }
        var remainingAttempts by rememberSaveable {
            mutableIntStateOf(3)
        }
        var showWrongPinTitle by rememberSaveable {
            mutableStateOf(false)
        }
        var showSignOutPrompt by rememberSaveable {
            mutableStateOf(false)
        }

        fun handleEvents(event: PinUnlockEvents) {
            when (event) {
                is PinUnlockEvents.OnPinKeypadPressed -> {
                    pinEntry = pinEntry.process(event.pinKeypadModel)
                    if (pinEntry.isComplete()) {
                        coroutineScope.launch { pinStateService.unlock() }
                    }
                }
                PinUnlockEvents.OnForgetPin -> showSignOutPrompt = true
                PinUnlockEvents.ClearSignOutPrompt -> showSignOutPrompt = false
            }
        }
        return PinUnlockState(
            pinEntry = pinEntry,
            showWrongPinTitle = showWrongPinTitle,
            remainingAttempts = remainingAttempts,
            showSignOutPrompt = showSignOutPrompt,
            eventSink = ::handleEvents
        )
    }

    private fun PinEntry.process(pinKeypadModel: PinKeypadModel): PinEntry {
        return when (pinKeypadModel) {
            PinKeypadModel.Back -> deleteLast()
            is PinKeypadModel.Number -> addDigit(pinKeypadModel.number)
            PinKeypadModel.Empty -> this
        }
    }
}
