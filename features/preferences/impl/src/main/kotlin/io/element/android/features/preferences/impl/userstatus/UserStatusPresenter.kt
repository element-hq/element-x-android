/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.launch

@Inject
class UserStatusPresenter(
    private val matrixClient: MatrixClient,
) : Presenter<UserStatusState> {
    @Composable
    override fun present(): UserStatusState {
        val userProfile by matrixClient.userProfile.collectAsState()
        var pickerState by remember { mutableStateOf<UserStatusPickerState>(UserStatusPickerState.Hidden) }
        val customTextFieldState = rememberTextFieldState()
        val coroutineScope = rememberCoroutineScope()

        fun handleEvent(event: UserStatusEvent) {
            when (event) {
                UserStatusEvent.OpenPicker -> pickerState = UserStatusPickerState.ShowingPicker
                UserStatusEvent.DismissPicker -> pickerState = UserStatusPickerState.Hidden
                UserStatusEvent.OpenCustomInput -> {
                    val raw = userProfile.rawStatus
                    if (raw != null) {
                        customTextFieldState.setTextAndPlaceCursorAtEnd(raw.text)
                    } else {
                        customTextFieldState.clearText()
                    }
                    pickerState = UserStatusPickerState.CustomInput(
                        emoji = raw?.emoji ?: "😀",
                        textFieldState = customTextFieldState,
                    )
                }
                is UserStatusEvent.SetStatus -> {
                    pickerState = UserStatusPickerState.Hidden
                    coroutineScope.launch { matrixClient.setUserStatus(event.status) }
                }
                UserStatusEvent.ClearStatus -> {
                    pickerState = UserStatusPickerState.Hidden
                    coroutineScope.launch { matrixClient.clearUserStatus() }
                }
                UserStatusEvent.CancelCustomInput -> pickerState = UserStatusPickerState.Hidden
                is UserStatusEvent.UpdateCustomEmoji -> {
                    val current = pickerState as? UserStatusPickerState.CustomInput ?: return
                    pickerState = current.copy(emoji = event.emoji)
                }
            }
        }

        return UserStatusState(
            displayedStatus = userProfile.displayedStatus,
            rawStatus = userProfile.rawStatus,
            pickerState = pickerState,
            eventSink = ::handleEvent,
        )
    }
}
