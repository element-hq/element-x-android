/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus

internal class UserStatusStateProvider : PreviewParameterProvider<UserStatusState> {
    override val values get() = sequenceOf(
        aUserStatusState(),
        aUserStatusState(displayedStatus = DisplayedStatus.UserSet(UserStatus("🌴", "Away"))),
        aUserStatusState(displayedStatus = DisplayedStatus.InCall(callJoinedTs = 0L)),
        aUserStatusState(pickerState = UserStatusPickerState.ShowingPicker),
        aUserStatusState(
            displayedStatus = DisplayedStatus.UserSet(UserStatus("🌴", "Away")),
            rawStatus = UserStatus("🌴", "Away"),
            pickerState = UserStatusPickerState.ShowingPicker,
        ),
        aUserStatusState(pickerState = UserStatusPickerState.CustomInput(emoji = "😀", textFieldState = TextFieldState())),
        aUserStatusState(pickerState = UserStatusPickerState.CustomInput(emoji = "🚀", textFieldState = TextFieldState("Working on something"))),
    )
}

fun aUserStatusState(
    displayedStatus: DisplayedStatus? = null,
    rawStatus: UserStatus? = null,
    pickerState: UserStatusPickerState = UserStatusPickerState.Hidden,
    eventSink: (UserStatusEvent) -> Unit = {},
) = UserStatusState(
    displayedStatus = displayedStatus,
    rawStatus = rawStatus,
    pickerState = pickerState,
    eventSink = eventSink,
)
