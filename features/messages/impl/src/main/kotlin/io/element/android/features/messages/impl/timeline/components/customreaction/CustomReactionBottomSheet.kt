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

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.emojibasebindings.Emoji
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.api.core.EventId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReactionBottomSheet(
    state: CustomReactionState,
    onEmojiSelected: (EventId, Emoji) -> Unit,
    onReactionSelected: (EventId, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = state.searchState.startActive)
    val coroutineScope = rememberCoroutineScope()
    val target = state.target as? CustomReactionState.Target.Success
    val localView = LocalView.current

    fun onDismiss() {
        localView.hideKeyboard()
        state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
    }

    fun onEmojiSelectedDismiss(emoji: Emoji) {
        localView.hideKeyboard()
        if (target?.event?.eventId == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            onEmojiSelected(target.event.eventId, emoji)
        }
    }

    fun onReactionSelectedDismiss(reaction: String) {
        if (target?.event?.eventId == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            onReactionSelected(target.event.eventId, reaction)
        }
    }

    if (target?.emojibaseStore != null && target.event.eventId != null) {
        ModalBottomSheet(
            onDismissRequest = ::onDismiss,
            sheetState = sheetState,
            modifier = modifier
                .heightIn(min = if (state.searchState.isSearchActive) (LocalConfiguration.current.screenHeightDp).dp else Dp.Unspecified)
                .pointerInput(state.searchState.isSearchActive) {
                    awaitEachGesture {
                        // For any unconsumed pointer event in this sheet, deactivate the search field and hide the keyboard
                        awaitFirstDown(requireUnconsumed = true)
                        if (state.searchState.isSearchActive) {
                            state.searchState.eventSink(EmojiPickerEvents.OnSearchActiveChanged(false))
                        }
                    }
                }
        ) {
            EmojiPicker(
                onEmojiSelected = ::onEmojiSelectedDismiss,
                onReactionSelected = ::onReactionSelectedDismiss,
                emojibaseStore = target.emojibaseStore,
                selectedEmojis = state.selectedEmoji,
                state = state.searchState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
