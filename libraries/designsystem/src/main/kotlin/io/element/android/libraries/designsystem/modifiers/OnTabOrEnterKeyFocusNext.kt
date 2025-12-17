/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.onTabOrEnterKeyFocusNext(focusManager: FocusManager): Modifier = onPreviewKeyEvent { event ->
    if (event.key == Key.Tab || event.key == Key.Enter) {
        if (event.type == KeyEventType.KeyUp) {
            focusManager.moveFocus(FocusDirection.Down)
        }
        true
    } else {
        false
    }
}
