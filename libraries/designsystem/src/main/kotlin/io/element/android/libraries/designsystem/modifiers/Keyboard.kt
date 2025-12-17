/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Modifier to handle Shift + F10 key events.
 * This is typically used to trigger context menus in desktop applications.
 *
 * @param action The callback to invoke when Shift + F10 is pressed.
 */
fun Modifier.onKeyboardContextMenuAction(
    action: (() -> Unit)?,
): Modifier = then(
    if (action == null) {
        Modifier
    } else {
        Modifier.onKeyEvent { keyEvent ->
            // invoke the callback when the user presses Shift + F10
            if (keyEvent.type == KeyEventType.KeyUp &&
                keyEvent.isShiftPressed &&
                keyEvent.key == Key.F10) {
                action()
                true
            } else {
                false
            }
        }
    }
)
