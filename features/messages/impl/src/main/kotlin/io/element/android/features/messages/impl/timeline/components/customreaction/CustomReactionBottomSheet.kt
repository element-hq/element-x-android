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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.api.core.EventId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReactionBottomSheet(
    state: CustomReactionState,
    onSelectEmoji: (EventId, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val target = state.target as? CustomReactionState.Target.Success

    fun onDismiss() {
        state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
    }

    fun onEmojiSelectedDismiss(emoji: String) {
        if (target?.event?.eventId == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            onSelectEmoji(target.event.eventId, emoji)
        }
    }

    if (target?.emojibaseStore != null && target.event.eventId != null) {
        ModalBottomSheet(
            onDismissRequest = ::onDismiss,
            sheetState = sheetState,
            modifier = modifier
        ) {
            EmojiPicker(
                onSelectEmoji = ::onEmojiSelectedDismiss,
                emojibaseStore = target.emojibaseStore,
                selectedEmojis = state.selectedEmoji,
                skinTone = state.skinTone,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
