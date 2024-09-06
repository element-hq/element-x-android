/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

fun Modifier.clickableIfNotNull(onClick: (() -> Unit)? = null): Modifier = then(
    if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }
)
