/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.user.UserStatus
import kotlinx.coroutines.launch

@ContributesBinding(SessionScope::class)
class UserStatusPresenter(
    private val matrixClient: MatrixClient,
) : Presenter<UserStatusState> {

    @Composable
    override fun present(): UserStatusState {
        val userProfile by matrixClient.userProfile.collectAsState()
        var pickerState by remember { mutableStateOf<UserStatusPickerState>(UserStatusPickerState.Hidden) }
        val coroutineScope = rememberCoroutineScope()

        fun handleEvent(event: UserStatusEvent) {
            when (event) {
                UserStatusEvent.Open -> pickerState = UserStatusPickerState.ShowingPicker
                UserStatusEvent.Dismiss -> pickerState = UserStatusPickerState.Hidden
                UserStatusEvent.OpenCustomInput -> {
                    val raw = userProfile.rawStatus
                    pickerState = UserStatusPickerState.CustomInput(
                        emoji = raw?.emoji ?: "😀",
                        text = raw?.text ?: "",
                    )
                }
                is UserStatusEvent.Set -> {
                    pickerState = UserStatusPickerState.Hidden
                    coroutineScope.launch { matrixClient.setUserStatus(event.status) }
                }
                UserStatusEvent.Clear -> {
                    pickerState = UserStatusPickerState.Hidden
                    coroutineScope.launch { matrixClient.clearUserStatus() }
                }
                UserStatusEvent.CancelCustomInput -> pickerState = UserStatusPickerState.Hidden
                is UserStatusEvent.UpdateCustomEmoji -> {
                    val current = pickerState as? UserStatusPickerState.CustomInput ?: return
                    pickerState = current.copy(emoji = event.emoji)
                }
                is UserStatusEvent.UpdateCustomText -> {
                    val current = pickerState as? UserStatusPickerState.CustomInput ?: return
                    pickerState = current.copy(text = event.text)
                }
            }
        }

        return UserStatusState(
            displayedStatus = userProfile.displayedStatus,
            pickerState = pickerState,
            eventSink = ::handleEvent,
        )
    }
}
