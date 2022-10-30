package io.element.android.x.features.roomlist.model

import androidx.compose.runtime.Stable

@Stable
data class MatrixUser(
    val username: String? = null,
    val avatarUrl: String? = null,
    val avatarData: List<UByte>? = null,
)
