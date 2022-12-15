package io.element.android.x.features.messages.textcomposer

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.data.StableCharSequence
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.AppScope
import io.element.android.x.matrix.Matrix

@ContributesViewModel(AppScope::class)
class MessageComposerViewModel @AssistedInject constructor(
    private val matrix: Matrix,
    @Assisted private val initialState: MessageComposerViewState
) : MavericksViewModel<MessageComposerViewState>(initialState) {

    companion object :
        MavericksViewModelFactory<MessageComposerViewModel, MessageComposerViewState> by daggerMavericksViewModelFactory()

    private val client = matrix.activeClient()

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