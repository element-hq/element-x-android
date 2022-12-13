package io.element.android.x.designsystem.components.avatar

import androidx.compose.ui.unit.dp

enum class AvatarSize(val value: Int) {
    SMALL(32),
    MEDIUM(40),
    BIG(48);

    val dp = value.dp
}
