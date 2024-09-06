/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock.keypad

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PinKeypadModel {
    data object Empty : PinKeypadModel
    data object Back : PinKeypadModel
    data class Number(val number: Char) : PinKeypadModel
}
