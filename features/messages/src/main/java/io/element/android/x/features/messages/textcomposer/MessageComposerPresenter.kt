package io.element.android.x.features.messages.textcomposer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Presenter
import io.element.android.x.core.data.toStableCharSequence
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.room.MatrixRoom
import io.element.android.x.textcomposer.MessageComposerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageComposerPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val client: MatrixClient,
    private val room: MatrixRoom
) : Presenter<MessageComposerState> {

    @Composable
    override fun present(): MessageComposerState {
        val isFullScreen = rememberSaveable {
            mutableStateOf(false)
        }
        val text: MutableState<CharSequence> = rememberSaveable {
            mutableStateOf("")
        }
        val composerMode: MutableState<MessageComposerMode> = rememberSaveable {
            mutableStateOf(MessageComposerMode.Normal(""))
        }

        fun handleEvents(event: MessageComposerEvents) {
            when (event) {
                MessageComposerEvents.ToggleFullScreenState -> isFullScreen.value = !isFullScreen.value
                is MessageComposerEvents.UpdateText -> text.value = event.text
                MessageComposerEvents.CloseSpecialMode -> composerMode.setToNormal()
                is MessageComposerEvents.SendMessage -> appCoroutineScope.sendMessage(event.message, composerMode)
                is MessageComposerEvents.SetMode -> composerMode.value = event.composerMode
            }
        }

        return MessageComposerState(
            text = text.value.toStableCharSequence(),
            isFullScreen = isFullScreen.value,
            mode = composerMode.value,
            eventSink = ::handleEvents
        )
    }

    private fun MutableState<MessageComposerMode>.setToNormal() {
        value = MessageComposerMode.Normal("")
    }

    private fun CoroutineScope.sendMessage(text: String, composerMode: MutableState<MessageComposerMode>) = launch {
        val capturedMode = composerMode.value
        // Reset composer right away
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
