package io.element.android.x.features.messages.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import io.element.android.x.designsystem.VectorIcons

@Stable
sealed class MessagesItemAction(
    val title: String,
    @DrawableRes val icon: Int,
    val destructive: Boolean = false
) {
    object Forward : MessagesItemAction("Forward", VectorIcons.ArrowForward)
    object Copy : MessagesItemAction("Copy", VectorIcons.Copy)
    object Redact : MessagesItemAction("Redact", VectorIcons.Delete, destructive = true)
    object Reply : MessagesItemAction("Reply", VectorIcons.Reply)
    object Edit : MessagesItemAction("Edit", VectorIcons.Edit)
}