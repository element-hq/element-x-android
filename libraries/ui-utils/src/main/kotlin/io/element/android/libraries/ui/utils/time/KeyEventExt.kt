/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.time

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key

/**
 * Extension property to get the digit character from a KeyEvent.
 * This handles both regular digit keys and numpad keys.
 */
val KeyEvent.digit: Char? get() {
    val char = nativeKeyEvent.unicodeChar.toChar()
    return when {
        Character.isDigit(char) -> char
        key == Key.NumPad0 -> '0'
        key == Key.NumPad1 -> '1'
        key == Key.NumPad2 -> '2'
        key == Key.NumPad3 -> '3'
        key == Key.NumPad4 -> '4'
        key == Key.NumPad5 -> '5'
        key == Key.NumPad6 -> '6'
        key == Key.NumPad7 -> '7'
        key == Key.NumPad8 -> '8'
        key == Key.NumPad9 -> '9'
        else -> null
    }
}
