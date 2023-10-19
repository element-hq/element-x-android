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

package io.element.android.features.lockscreen.impl.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.lockscreen.impl.create.model.PinEntry
import io.element.android.features.lockscreen.impl.create.validation.CreatePinFailure
import io.element.android.features.lockscreen.impl.create.validation.PinValidator
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

private const val PIN_SIZE = 4

class CreatePinPresenter @Inject constructor(
    private val pinValidator: PinValidator,
) : Presenter<CreatePinState> {

    @Composable
    override fun present(): CreatePinState {
        var choosePinEntry by remember {
            mutableStateOf(PinEntry.empty(PIN_SIZE))
        }
        var confirmPinEntry by remember {
            mutableStateOf(PinEntry.empty(PIN_SIZE))
        }
        var isConfirmationStep by remember {
            mutableStateOf(false)
        }
        var createPinFailure by remember {
            mutableStateOf<CreatePinFailure?>(null)
        }

        fun handleEvents(event: CreatePinEvents) {
            when (event) {
                is CreatePinEvents.OnPinEntryChanged -> {
                    if (isConfirmationStep) {
                        confirmPinEntry = confirmPinEntry.fillWith(event.entryAsText)
                        if (confirmPinEntry.isPinComplete()) {
                            if (confirmPinEntry == choosePinEntry) {
                                //TODO save in db and navigate to next screen
                            } else {
                                createPinFailure = CreatePinFailure.ConfirmationPinNotMatching
                            }
                        }
                    } else {
                        choosePinEntry = choosePinEntry.fillWith(event.entryAsText)
                        if (choosePinEntry.isPinComplete()) {
                            when (val pinValidationResult = pinValidator.isPinValid(choosePinEntry)) {
                                is PinValidator.Result.Invalid -> {
                                    createPinFailure = pinValidationResult.failure
                                }
                                PinValidator.Result.Valid -> isConfirmationStep = true
                            }
                        }
                    }
                }
                CreatePinEvents.ClearFailure -> {
                    when (createPinFailure) {
                        is CreatePinFailure.ConfirmationPinNotMatching -> {
                            choosePinEntry = PinEntry.empty(PIN_SIZE)
                            confirmPinEntry = PinEntry.empty(PIN_SIZE)
                        }
                        is CreatePinFailure.ChosenPinBlacklisted -> {
                            choosePinEntry = PinEntry.empty(PIN_SIZE)
                        }
                        null -> Unit
                    }
                    isConfirmationStep = false
                    createPinFailure = null
                }
            }
        }

        return CreatePinState(
            choosePinEntry = choosePinEntry,
            confirmPinEntry = confirmPinEntry,
            isConfirmationStep = isConfirmationStep,
            createPinFailure = createPinFailure,
            eventSink = ::handleEvents
        )
    }
}
