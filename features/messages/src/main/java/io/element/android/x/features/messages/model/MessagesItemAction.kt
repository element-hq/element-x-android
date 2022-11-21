package io.element.android.x.features.messages.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import io.element.android.x.designsystem.VectorIcons

@Stable
sealed class MessagesItemAction(val title: String, @DrawableRes val icon: Int) {
    object Forward : MessagesItemAction("Forward", VectorIcons.ArrowForward)
    object Copy : MessagesItemAction("Copy", VectorIcons.Copy)
}