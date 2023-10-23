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

package io.element.android.features.lockscreen.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.appconfig.LockScreenConfig
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class LockScreenSettingsPresenter @Inject constructor() : Presenter<LockScreenSettingsState> {

    @Composable
    override fun present(): LockScreenSettingsState {

        var isBiometricEnabled by remember {
            mutableStateOf(false)
        }
        var showRemovePinConfirmation by remember {
            mutableStateOf(false)
        }

        fun handleEvents(event: LockScreenSettingsEvents) {
            when (event) {
                LockScreenSettingsEvents.CancelRemovePin -> TODO()
                LockScreenSettingsEvents.ChangePin -> TODO()
                LockScreenSettingsEvents.ConfirmRemovePin -> TODO()
                LockScreenSettingsEvents.RemovePin -> TODO()
                LockScreenSettingsEvents.ToggleBiometric -> TODO()
            }
        }

        return LockScreenSettingsState(
            isPinMandatory = LockScreenConfig.IS_PIN_MANDATORY,
            isBiometricEnabled = isBiometricEnabled,
            showRemovePinConfirmation = showRemovePinConfirmation,
            eventSink = ::handleEvents
        )
    }
}
