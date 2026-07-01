/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.text

import androidx.compose.ui.text.style.TextDirection

/**
 * Detect if a text is RTL or not, based on its content.
 */
fun TextDirection.Companion.detect(text: String): TextDirection {
    return if (text.any(::isRtlChar)) TextDirection.Rtl else TextDirection.Ltr
}

/**
 * Detect if a character is an RTL character or not, based on its Unicode directionality.
 */
fun isRtlChar(char: Char): Boolean = when (Character.getDirectionality(char)) {
    Character.DIRECTIONALITY_RIGHT_TO_LEFT,
    Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
    Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
    Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE -> true
    else -> false
}
