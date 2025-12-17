/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

fun Modifier.clickableIfNotNull(onClick: (() -> Unit)? = null): Modifier = this.then(
    if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
)

fun Modifier.niceClickable(
    onClick: () -> Unit,
): Modifier {
    return clip(RoundedCornerShape(4.dp))
        .clickable { onClick() }
        .padding(horizontal = 4.dp)
}
