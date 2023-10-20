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

import io.element.android.features.lockscreen.impl.create.model.PinEntry
import io.element.android.features.lockscreen.impl.create.validation.CreatePinFailure

data class CreatePinState(
    val choosePinEntry: PinEntry,
    val confirmPinEntry: PinEntry,
    val isConfirmationStep: Boolean,
    val createPinFailure: CreatePinFailure?,
    val appName: String,
    val eventSink: (CreatePinEvents) -> Unit
) {
    val pinSize = choosePinEntry.size
    val activePinEntry = if (isConfirmationStep) {
        confirmPinEntry
    } else {
        choosePinEntry
    }
}
