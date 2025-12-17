/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun AvatarType.User.avatarShape() = CircleShape

@Composable
fun AvatarType.Room.avatarShape() = CircleShape

@Composable
fun AvatarType.Space.avatarShape(avatarSize: Dp) = RoundedCornerShape(avatarSize * 0.25f)

@Composable
fun AvatarType.avatarShape(avatarSize: Dp): Shape {
    return when (this) {
        is AvatarType.Space -> avatarShape(avatarSize)
        is AvatarType.Room -> avatarShape()
        is AvatarType.User -> avatarShape()
    }
}
