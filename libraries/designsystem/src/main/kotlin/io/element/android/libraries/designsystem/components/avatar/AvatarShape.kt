/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

@Composable
fun AvatarType.avatarShape(): Shape {
    return when (this) {
        is AvatarType.Space -> RoundedCornerShape(cornerSize)
        is AvatarType.Room,
        AvatarType.User -> CircleShape
    }
}
