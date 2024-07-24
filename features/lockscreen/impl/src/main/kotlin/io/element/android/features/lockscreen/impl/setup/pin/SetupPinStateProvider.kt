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

package io.element.android.features.lockscreen.impl.setup.pin

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.lockscreen.impl.pin.model.PinEntry
import io.element.android.features.lockscreen.impl.setup.pin.validation.SetupPinFailure

open class SetupPinStateProvider : PreviewParameterProvider<SetupPinState> {
    override val values: Sequence<SetupPinState>
        get() = sequenceOf(
            aSetupPinState(),
            aSetupPinState(
                choosePinEntry = PinEntry.createEmpty(4).fillWith("12")
            ),
            aSetupPinState(
                choosePinEntry = PinEntry.createEmpty(4).fillWith("1789"),
                isConfirmationStep = true,
            ),
            aSetupPinState(
                choosePinEntry = PinEntry.createEmpty(4).fillWith("1789"),
                confirmPinEntry = PinEntry.createEmpty(4).fillWith("1788"),
                isConfirmationStep = true,
                creationFailure = SetupPinFailure.PinsDoNotMatch
            ),
            aSetupPinState(
                choosePinEntry = PinEntry.createEmpty(4).fillWith("1111"),
                creationFailure = SetupPinFailure.ForbiddenPin
            ),
        )
}

fun aSetupPinState(
    choosePinEntry: PinEntry = PinEntry.createEmpty(4),
    confirmPinEntry: PinEntry = PinEntry.createEmpty(4),
    isConfirmationStep: Boolean = false,
    creationFailure: SetupPinFailure? = null,
) = SetupPinState(
    choosePinEntry = choosePinEntry,
    confirmPinEntry = confirmPinEntry,
    isConfirmationStep = isConfirmationStep,
    setupPinFailure = creationFailure,
    appName = "Element",
    eventSink = {}
)
