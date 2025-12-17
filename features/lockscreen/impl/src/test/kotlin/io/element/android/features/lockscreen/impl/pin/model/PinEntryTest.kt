/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PinEntryTest {
    @Test
    fun `when using fillWith with empty string ensure pin is empty`() {
        val pinEntry = PinEntry.createEmpty(4)
        val newPinEntry = pinEntry.fillWith("")
        assertThat(newPinEntry.isEmpty()).isTrue()
    }

    @Test
    fun `when using fillWith with bigger string than size ensure pin is complete`() {
        val pinEntry = PinEntry.createEmpty(4)
        val newPinEntry = pinEntry.fillWith("12345")
        assertThat(newPinEntry.isComplete()).isTrue()
        newPinEntry.assertText("1234")
    }

    @Test
    fun `when using fillWith with non digit string ensure pin is filtering`() {
        val pinEntry = PinEntry.createEmpty(4)
        val newPinEntry = pinEntry.fillWith("12aa")
        newPinEntry.assertText("12")
    }

    @Test
    fun `when using clear ensure pin is empty`() {
        val pinEntry = PinEntry.createEmpty(4)
        val newPinEntry = pinEntry.clear()
        assertThat(newPinEntry.isEmpty()).isTrue()
        assertThat(newPinEntry.isComplete()).isFalse()
        newPinEntry.assertText("")
    }

    @Test
    fun `when using deleteLast ensure pin correct`() {
        val pinEntry = PinEntry.createEmpty(4)
        val newPinEntry = pinEntry.fillWith("1234").deleteLast()
        newPinEntry.assertText("123")
    }

    @Test
    fun `when using deleteLast with empty pin ensure pin is empty`() {
        val pinEntry = PinEntry.createEmpty(4)
        val newPinEntry = pinEntry.deleteLast()
        assertThat(newPinEntry.isEmpty()).isTrue()
    }

    @Test
    fun `when using addDigit with complete pin ensure pin is complete`() {
        val pinEntry = PinEntry.createEmpty(4)
        val newPinEntry = pinEntry
            .addDigit('1')
            .addDigit('2')
            .addDigit('3')
            .addDigit('4')
            .addDigit('5')
        assertThat(newPinEntry.isComplete()).isTrue()
        newPinEntry.assertText("1234")
    }
}
