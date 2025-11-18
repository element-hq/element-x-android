/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteSizeTest {
    @Test
    fun testSizeConversions() {
        // Check bytes to other units
        val bytes = 10_000_000.bytes
        assertThat(bytes.to(ByteUnit.BYTES)).isEqualTo(bytes.value)
        assertThat(bytes.to(ByteUnit.KB)).isEqualTo(bytes.value / 1024L)
        assertThat(bytes.to(ByteUnit.MB)).isEqualTo(bytes.value / 1024L / 1024L)
        assertThat(bytes.to(ByteUnit.GB)).isEqualTo(bytes.value / 1024L / 1024L / 1024L)

        // Now check for values too small to be converted
        assertThat(100.bytes.to(ByteUnit.KB)).isEqualTo(0)
        assertThat(100.bytes.to(ByteUnit.MB)).isEqualTo(0)
        assertThat(100.bytes.to(ByteUnit.GB)).isEqualTo(0)

        // Check for KBs
        val kiloBytes = 10_000.kiloBytes
        assertThat(kiloBytes.to(ByteUnit.BYTES)).isEqualTo(kiloBytes.value * 1024L)
        assertThat(kiloBytes.to(ByteUnit.KB)).isEqualTo(kiloBytes.value)
        assertThat(kiloBytes.to(ByteUnit.MB)).isEqualTo(kiloBytes.value / 1024L)
        assertThat(kiloBytes.to(ByteUnit.GB)).isEqualTo(kiloBytes.value / 1024L / 1024L)

        // Check for MBs
        val megaBytes = 10_000.megaBytes
        assertThat(megaBytes.to(ByteUnit.BYTES)).isEqualTo(megaBytes.value * 1024L * 1024L)
        assertThat(megaBytes.to(ByteUnit.KB)).isEqualTo(megaBytes.value * 1024L)
        assertThat(megaBytes.to(ByteUnit.MB)).isEqualTo(megaBytes.value)
        assertThat(megaBytes.to(ByteUnit.GB)).isEqualTo(megaBytes.value / 1024L)

        // Check for GBs
        val gigaBytes = 10.gigaBytes
        assertThat(gigaBytes.to(ByteUnit.BYTES)).isEqualTo(gigaBytes.value * 1024L * 1024L * 1024L)
        assertThat(gigaBytes.to(ByteUnit.KB)).isEqualTo(gigaBytes.value * 1024L * 1024L)
        assertThat(gigaBytes.to(ByteUnit.MB)).isEqualTo(gigaBytes.value * 1024L)
        assertThat(gigaBytes.to(ByteUnit.GB)).isEqualTo(gigaBytes.value)
    }
}
