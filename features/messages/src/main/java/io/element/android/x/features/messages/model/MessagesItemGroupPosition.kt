package io.element.android.x.features.messages.model

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

sealed interface MessagesItemGroupPosition {
    object First : MessagesItemGroupPosition
    object Middle : MessagesItemGroupPosition
    object Last : MessagesItemGroupPosition
    object None : MessagesItemGroupPosition

    fun isNew(): Boolean = when (this) {
        First, None -> true
        else -> false
    }

}

internal class MessagesItemGroupPositionProvider : PreviewParameterProvider<MessagesItemGroupPosition> {
    override val values = sequenceOf(
        MessagesItemGroupPosition.First,
        MessagesItemGroupPosition.Middle,
        MessagesItemGroupPosition.Last,
        MessagesItemGroupPosition.None,
    )
}