/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.tools

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecoveryKeyToolsTest {
    @Test
    fun `isRecoveryKeyFormatValid return false for invalid key`() {
        val sut = RecoveryKeyTools()
        assertThat(sut.isRecoveryKeyFormatValid("")).isFalse()
        // Wrong size
        assertThat(sut.isRecoveryKeyFormatValid("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabc")).isFalse()
        assertThat(sut.isRecoveryKeyFormatValid("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcda")).isFalse()
        // Wrong alphabet 0
        assertThat(sut.isRecoveryKeyFormatValid("0bcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isFalse()
        // Wrong alphabet O
        assertThat(sut.isRecoveryKeyFormatValid("Obcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isFalse()
        // Wrong alphabet l
        assertThat(sut.isRecoveryKeyFormatValid("lbcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isFalse()
    }

    @Test
    fun `isRecoveryKeyFormatValid return true for valid key`() {
        val sut = RecoveryKeyTools()
        assertThat(sut.isRecoveryKeyFormatValid("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isTrue()
        // Spaces does not count
        assertThat(sut.isRecoveryKeyFormatValid("abcd abcd abcd abcd abcd abcd abcd abcd abcd abcd abcd abcd")).isTrue()
    }
}
