package io.element.android.x.features.messages.textcomposer

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import io.element.android.x.core.data.StableCharSequence
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance

class MessageComposerViewModel(
    private val client: MatrixClient,
    private val initialState: MessageComposerViewState
) : MavericksViewModel<MessageComposerViewState>(initialState) {

    companion object :
        MavericksViewModelFactory<MessageComposerViewModel, MessageComposerViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: MessageComposerViewState
        ): MessageComposerViewModel? {
            val matrix = MatrixInstance.getInstance()
            val client = matrix.activeClient()
            return MessageComposerViewModel(
                client,
                state
            )
        }
    }

    fun onComposerFullScreenChange() {
        setState {
            copy(
                isFullScreen = !isFullScreen
            )
        }
    }

    fun updateText(newText: CharSequence) {
        setState {
            copy(
                text = StableCharSequence(newText),
                isSendButtonVisible = newText.isNotEmpty(),
            )
        }
    }
}
