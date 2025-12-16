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
        assertThat(bytes.into(ByteUnit.BYTES)).isEqualTo(bytes.value)
        assertThat(bytes.into(ByteUnit.KB)).isEqualTo(bytes.value / 1024L)
        assertThat(bytes.into(ByteUnit.MB)).isEqualTo(bytes.value / 1024L / 1024L)
        assertThat(bytes.into(ByteUnit.GB)).isEqualTo(bytes.value / 1024L / 1024L / 1024L)

        // Now check for values too small to be converted
        assertThat(100.bytes.into(ByteUnit.KB)).isEqualTo(0)
        assertThat(100.bytes.into(ByteUnit.MB)).isEqualTo(0)
        assertThat(100.bytes.into(ByteUnit.GB)).isEqualTo(0)

        // Check for KBs
        val kiloBytes = 10_000.kiloBytes
        assertThat(kiloBytes.into(ByteUnit.BYTES)).isEqualTo(kiloBytes.value * 1024L)
        assertThat(kiloBytes.into(ByteUnit.KB)).isEqualTo(kiloBytes.value)
        assertThat(kiloBytes.into(ByteUnit.MB)).isEqualTo(kiloBytes.value / 1024L)
        assertThat(kiloBytes.into(ByteUnit.GB)).isEqualTo(kiloBytes.value / 1024L / 1024L)

        // Check for MBs
        val megaBytes = 10_000.megaBytes
        assertThat(megaBytes.into(ByteUnit.BYTES)).isEqualTo(megaBytes.value * 1024L * 1024L)
        assertThat(megaBytes.into(ByteUnit.KB)).isEqualTo(megaBytes.value * 1024L)
        assertThat(megaBytes.into(ByteUnit.MB)).isEqualTo(megaBytes.value)
        assertThat(megaBytes.into(ByteUnit.GB)).isEqualTo(megaBytes.value / 1024L)

        // Check for GBs
        val gigaBytes = 10.gigaBytes
        assertThat(gigaBytes.into(ByteUnit.BYTES)).isEqualTo(gigaBytes.value * 1024L * 1024L * 1024L)
        assertThat(gigaBytes.into(ByteUnit.KB)).isEqualTo(gigaBytes.value * 1024L * 1024L)
        assertThat(gigaBytes.into(ByteUnit.MB)).isEqualTo(gigaBytes.value * 1024L)
        assertThat(gigaBytes.into(ByteUnit.GB)).isEqualTo(gigaBytes.value)
    }
}
