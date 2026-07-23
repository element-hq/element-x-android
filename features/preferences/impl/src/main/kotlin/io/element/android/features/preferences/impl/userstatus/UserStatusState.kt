/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.userstatus

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.emoji.api.picker.EmojiPickerState
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus

data class UserStatusState(
    val displayedStatus: DisplayedStatus?,
    val rawStatus: UserStatus?,
    val pickerState: UserStatusPickerState,
    val eventSink: (UserStatusEvent) -> Unit,
)

@Immutable
sealed interface UserStatusPickerState {
    data object Hidden : UserStatusPickerState
    data object ShowingPicker : UserStatusPickerState
    data class CustomInput(
        val emoji: String,
        val textFieldState: TextFieldState,
        val emojiPickerSheetState: EmojiPickerSheetState,
    ) : UserStatusPickerState
}

@Immutable
sealed interface EmojiPickerSheetState {
    data object Hidden : EmojiPickerSheetState
    data object Loading : EmojiPickerSheetState
    data class Shown(val state: EmojiPickerState) : EmojiPickerSheetState
}

enum class PredefinedUserStatus(val emoji: String, @StringRes val labelRes: Int) {
    IN_A_MEETING("💬", R.string.screen_settings_user_status_in_a_meeting),
    FOCUS_TIME("💡", R.string.screen_settings_user_status_focus_time),
    ON_THE_ROAD("🚙", R.string.screen_settings_user_status_on_the_road),
    BE_RIGHT_BACK("☕", R.string.screen_settings_user_status_be_right_back),
    AWAY("🌴", R.string.screen_settings_user_status_away),
}
