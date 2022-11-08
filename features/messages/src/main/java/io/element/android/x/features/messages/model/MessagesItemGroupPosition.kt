package io.element.android.x.features.messages.model

sealed interface MessagesItemGroupPosition {
    object First : MessagesItemGroupPosition
    object Middle : MessagesItemGroupPosition
    object Last : MessagesItemGroupPosition
    object None : MessagesItemGroupPosition

}
