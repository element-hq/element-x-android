/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import io.element.android.libraries.matrix.api.user.DisplayedStatus

data class UserStatusState(
    val displayedStatus: DisplayedStatus?,
    val pickerState: UserStatusPickerState,
    val eventSink: (UserStatusEvent) -> Unit,
)

sealed interface UserStatusPickerState {
    data object Hidden : UserStatusPickerState
    data object ShowingPicker : UserStatusPickerState
    data class CustomInput(
        val initialEmoji: String = "😀",
        val initialText: String = "",
    ) : UserStatusPickerState
}
