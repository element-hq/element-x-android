package io.element.android.x.features.roomlist.model

import androidx.compose.runtime.Stable
import io.element.android.x.designsystem.components.avatar.AvatarData

@Stable
data class MatrixUser(
    val username: String? = null,
    val avatarUrl: String? = null,
    val avatarData: AvatarData = AvatarData(),
)
