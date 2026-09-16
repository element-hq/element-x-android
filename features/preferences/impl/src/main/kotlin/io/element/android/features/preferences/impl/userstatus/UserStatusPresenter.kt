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
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.emoji.api.picker.EmojiPickerPresenter
import io.element.android.libraries.emoji.api.recentemojis.EmptyGetRecentEmojis
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.launch

@Inject
class UserStatusPresenter(
    private val matrixClient: MatrixClient,
    emojiPickerPresenterFactory: EmojiPickerPresenter.Factory,
) : Presenter<UserStatusState> {
    private val emojiPickerPresenter = emojiPickerPresenterFactory.create(EmptyGetRecentEmojis)

    @Composable
    override fun present(): UserStatusState {
        val userProfile by matrixClient.userProfile.collectAsState()
        var pickerState by remember { mutableStateOf<UserStatusPickerState>(UserStatusPickerState.Hidden) }
        var isEmojiPickerVisible by remember { mutableStateOf(false) }
        val customTextFieldState = rememberTextFieldState()
        val coroutineScope = rememberCoroutineScope()
        val updateStatusAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        fun handleEvent(event: UserStatusEvent) {
            when (event) {
                // Mode transitions
                UserStatusEvent.OpenPicker -> pickerState = UserStatusPickerState.ShowingPicker
                UserStatusEvent.DismissPicker -> pickerState = UserStatusPickerState.Hidden
                UserStatusEvent.CancelCustomInput -> pickerState = UserStatusPickerState.Hidden
                is UserStatusEvent.SetStatus -> {
                    pickerState = UserStatusPickerState.Hidden
                    coroutineScope.launch {
                        updateStatusAction.runUpdatingState { matrixClient.setUserStatus(event.status) }
                    }
                }
                UserStatusEvent.ClearStatus -> {
                    pickerState = UserStatusPickerState.Hidden
                    coroutineScope.launch {
                        updateStatusAction.runUpdatingState { matrixClient.clearUserStatus() }
                    }
                }
                UserStatusEvent.OpenCustomInput -> {
                    val raw = userProfile.rawStatus
                    if (raw != null) {
                        customTextFieldState.setTextAndPlaceCursorAtEnd(raw.text)
                    } else {
                        customTextFieldState.clearText()
                    }
                    isEmojiPickerVisible = false
                    pickerState = UserStatusPickerState.CustomInput(
                        emoji = raw?.emoji ?: "😀",
                        textFieldState = customTextFieldState,
                        emojiPickerSheetState = EmojiPickerSheetState.Hidden,
                    )
                }
                // Custom-input mutations
                is UserStatusEvent.UpdateCustomEmoji -> {
                    isEmojiPickerVisible = false
                    val current = pickerState as? UserStatusPickerState.CustomInput ?: return
                    pickerState = current.copy(emoji = event.emoji)
                }
                // Emoji-picker visibility (only meaningful within CustomInput mode)
                UserStatusEvent.OpenEmojiPicker -> isEmojiPickerVisible = true
                UserStatusEvent.DismissEmojiPicker -> isEmojiPickerVisible = false
            }
        }

        // In CustomInput mode, override the picker state's `emojiPickerSheetState` with the freshly composed emoji-picker
        // sub-state so recompositions of the picker propagate up. Other modes pass through unchanged.
        val effectivePickerState = when (val current = pickerState) {
            is UserStatusPickerState.CustomInput -> {
                val emojiPickerState = presentEmojiPickerSheetState(isEmojiPickerVisible)
                current.copy(emojiPickerSheetState = emojiPickerState)
            }
            else -> current
        }

        return UserStatusState(
            displayedStatus = userProfile.displayedStatus,
            rawStatus = userProfile.rawStatus,
            pickerState = effectivePickerState,
            updateStatusAction = updateStatusAction.value,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun presentEmojiPickerSheetState(isVisible: Boolean): EmojiPickerSheetState {
        if (!isVisible) return EmojiPickerSheetState.Hidden
        val emojiPickerState = emojiPickerPresenter.present()
        return if (emojiPickerState.isReady) {
            EmojiPickerSheetState.Shown(emojiPickerState)
        } else {
            EmojiPickerSheetState.Loading
        }
    }
}
