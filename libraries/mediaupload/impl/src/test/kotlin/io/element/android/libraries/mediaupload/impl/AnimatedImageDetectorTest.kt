/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * These tests build minimal, synthetic-but-spec-valid byte streams for each format rather than relying on real
 * image files, so that the parsing logic can be exercised precisely (including edge cases like a single-frame GIF)
 * without needing binary test assets or an image codec.
 */
class AnimatedImageDetectorTest {
    private val detector = AnimatedImageDetector()

    @Test
    fun `a single frame gif is not animated`() {
        val bytes = gif(frameCount = 1)
        assertThat(detector.isAnimated(bytes.inputStream())).isFalse()
    }

    @Test
    fun `a multi frame gif is animated`() {
        val bytes = gif(frameCount = 3)
        assertThat(detector.isAnimated(bytes.inputStream())).isTrue()
    }

    @Test
    fun `a gif with a global color table is still parsed correctly`() {
        val bytes = gif(frameCount = 2, globalColorTableSizeBits = 1)
        assertThat(detector.isAnimated(bytes.inputStream())).isTrue()
    }

    @Test
    fun `a simple lossy webp is not animated`() {
        val bytes = simpleWebP()
        assertThat(detector.isAnimated(bytes.inputStream())).isFalse()
    }

    @Test
    fun `an extended webp without the animation flag is not animated`() {
        val bytes = extendedWebP(animated = false)
        assertThat(detector.isAnimated(bytes.inputStream())).isFalse()
    }

    @Test
    fun `an extended webp with the animation flag is animated`() {
        val bytes = extendedWebP(animated = true)
        assertThat(detector.isAnimated(bytes.inputStream())).isTrue()
    }

    @Test
    fun `a plain png is not animated`() {
        val bytes = png(withActl = false)
        assertThat(detector.isAnimated(bytes.inputStream())).isFalse()
    }

    @Test
    fun `an apng with an acTL chunk before IDAT is animated`() {
        val bytes = png(withActl = true)
        assertThat(detector.isAnimated(bytes.inputStream())).isTrue()
    }

    @Test
    fun `a jpeg is not animated`() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        assertThat(detector.isAnimated(bytes.inputStream())).isFalse()
    }

    @Test
    fun `an empty stream is not animated`() {
        assertThat(detector.isAnimated(ByteArrayInputStream(ByteArray(0)))).isFalse()
    }

    // region Test fixtures

    private fun gif(frameCount: Int, globalColorTableSizeBits: Int = 0): ByteArray {
        val out = ByteArrayOutputStream()
        out.write("GIF89a".toByteArray(Charsets.US_ASCII))
        val hasGlobalColorTable = globalColorTableSizeBits > 0
        val packedFields = (if (hasGlobalColorTable) 0x80 else 0x00) or globalColorTableSizeBits
        // Logical Screen Descriptor: width(2) + height(2) + packed(1) + bg color index(1) + pixel aspect(1)
        out.write(le16(1))
        out.write(le16(1))
        out.write(packedFields)
        out.write(0)
        out.write(0)
        if (hasGlobalColorTable) {
            repeat(3 * (1 shl (globalColorTableSizeBits + 1))) { out.write(0) }
        }
        repeat(frameCount) {
            out.write(0x2C) // Image separator
            // Image descriptor: left(2) + top(2) + width(2) + height(2) + packed(1), no local color table
            out.write(le16(0))
            out.write(le16(0))
            out.write(le16(1))
            out.write(le16(1))
            out.write(0x00)
            out.write(0x02) // LZW minimum code size
            out.write(0x01) // sub-block of 1 byte
            out.write(0x00) // image data
            out.write(0x00) // block terminator
        }
        out.write(0x3B) // Trailer
        return out.toByteArray()
    }

    private fun simpleWebP(): ByteArray {
        val out = ByteArrayOutputStream()
        out.write("RIFF".toByteArray(Charsets.US_ASCII))
        out.write(le32(20))
        out.write("WEBP".toByteArray(Charsets.US_ASCII))
        out.write("VP8 ".toByteArray(Charsets.US_ASCII))
        out.write(le32(4))
        out.write(byteArrayOf(0, 0, 0, 0))
        return out.toByteArray()
    }

    private fun extendedWebP(animated: Boolean): ByteArray {
        val out = ByteArrayOutputStream()
        out.write("RIFF".toByteArray(Charsets.US_ASCII))
        out.write(le32(30))
        out.write("WEBP".toByteArray(Charsets.US_ASCII))
        out.write("VP8X".toByteArray(Charsets.US_ASCII))
        out.write(le32(10))
        val flags = if (animated) 0x02 else 0x00
        out.write(flags)
        repeat(9) { out.write(0) } // rest of the VP8X payload (reserved + canvas size)
        return out.toByteArray()
    }

    private fun png(withActl: Boolean): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        // IHDR chunk (13 bytes of dummy data)
        writeChunk(out, "IHDR", ByteArray(13))
        if (withActl) {
            // acTL chunk: num_frames(4) + num_plays(4)
            writeChunk(out, "acTL", be32(3) + be32(0))
        }
        writeChunk(out, "IDAT", ByteArray(4))
        writeChunk(out, "IEND", ByteArray(0))
        return out.toByteArray()
    }

    private fun writeChunk(out: ByteArrayOutputStream, type: String, data: ByteArray) {
        out.write(be32(data.size))
        out.write(type.toByteArray(Charsets.US_ASCII))
        out.write(data)
        out.write(ByteArray(4)) // dummy CRC, not validated by the detector
    }

    private fun le16(value: Int): ByteArray = byteArrayOf((value and 0xFF).toByte(), ((value shr 8) and 0xFF).toByte())

    private fun le32(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte(),
    )

    private fun be32(value: Int): ByteArray = byteArrayOf(
        ((value shr 24) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        (value and 0xFF).toByte(),
    )

    // endregion
}
