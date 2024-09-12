/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin.model

import com.google.common.truth.Truth.assertThat

fun PinEntry.assertText(text: String) {
    assertThat(toText()).isEqualTo(text)
}

fun PinEntry.assertEmpty() {
    val isEmpty = digits.all { it is PinDigit.Empty }
    assertThat(isEmpty).isTrue()
}
