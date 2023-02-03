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

package io.element.android.features.messages.textcomposer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.core.data.toStableCharSequence
import io.element.android.libraries.matrix.room.MatrixRoom
import io.element.android.libraries.textcomposer.MessageComposerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageComposerPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val room: MatrixRoom
) : Presenter<MessageComposerState> {

    @Composable
    override fun present(): MessageComposerState {
        val isFullScreen = rememberSaveable {
            mutableStateOf(false)
        }
        val text: MutableState<StableCharSequence> = remember {
            mutableStateOf(StableCharSequence(""))
        }
        val composerMode: MutableState<MessageComposerMode> = rememberSaveable {
            mutableStateOf(MessageComposerMode.Normal(""))
        }

        LaunchedEffect(composerMode.value) {
            when (val modeValue = composerMode.value) {
                is MessageComposerMode.Edit -> text.value = modeValue.defaultContent.toStableCharSequence()
                else -> Unit
            }
        }

        fun handleEvents(event: MessageComposerEvents) {
            when (event) {
                MessageComposerEvents.ToggleFullScreenState -> isFullScreen.value = !isFullScreen.value
                is MessageComposerEvents.UpdateText -> text.value = event.text.toStableCharSequence()
                MessageComposerEvents.CloseSpecialMode -> composerMode.setToNormal()
                is MessageComposerEvents.SendMessage -> appCoroutineScope.sendMessage(event.message, composerMode, text)
                is MessageComposerEvents.SetMode -> composerMode.value = event.composerMode
            }
        }

        return MessageComposerState(
            text = text.value,
            isFullScreen = isFullScreen.value,
            mode = composerMode.value,
            eventSink = ::handleEvents
        )
    }

    private fun MutableState<MessageComposerMode>.setToNormal() {
        value = MessageComposerMode.Normal("")
    }

    private fun CoroutineScope.sendMessage(text: String, composerMode: MutableState<MessageComposerMode>, textState: MutableState<StableCharSequence>) =
        launch {
            val capturedMode = composerMode.value
            // Reset composer right away
            textState.value = "".toStableCharSequence()
            composerMode.setToNormal()
            when (capturedMode) {
                is MessageComposerMode.Normal -> room.sendMessage(text)
                is MessageComposerMode.Edit -> room.editMessage(
                    capturedMode.eventId,
                    text
                )
                is MessageComposerMode.Quote -> TODO()
                is MessageComposerMode.Reply -> room.replyMessage(
                    capturedMode.eventId,
                    text
                )
            }
        }
}
