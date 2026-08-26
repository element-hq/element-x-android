/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import java.io.BufferedInputStream
import java.io.InputStream

/**
 * Inspects the raw bytes of an image to determine whether it is actually animated. GIF, WebP and PNG can all be
 * either static or animated, so this can't be reliably decided from the mime type alone:
 *  - most WebP images shared from a camera or gallery are static; treating every WebP as animated (as a mime-type
 *    based check would) skips a worthwhile compression for the common case;
 *  - a PNG can be animated too (APNG), which a check that only looks at GIF/WebP would miss entirely;
 *  - the declared, or even the resolved, mime type of an upload isn't always trustworthy (for example a GIF shared
 *    with a generic `image/*` mime type by an app that doesn't set it correctly), which could let an animated
 *    image slip through compression and silently lose its animation.
 *
 * Reading the file content directly avoids all of these pitfalls.
 */
class AnimatedImageDetector {
    /**
     * Returns `true` if [inputStream] contains an animated GIF, animated WebP, or animated PNG (APNG); `false` for
     * any other format, or if the content can't be parsed. The stream is consumed by this call.
     */
    fun isAnimated(inputStream: InputStream): Boolean {
        return runCatching {
            val stream = BufferedInputStream(inputStream, SIGNATURE_SIZE)
            stream.mark(SIGNATURE_SIZE)
            val signature = ByteArray(SIGNATURE_SIZE)
            val read = stream.readAtMost(signature)
            stream.reset()
            when {
                read >= 6 && (signature.startsWith(GIF_87A) || signature.startsWith(GIF_89A)) -> isAnimatedGif(stream)
                read >= SIGNATURE_SIZE && signature.startsWith(RIFF) && signature.regionMatches(8, WEBP) -> isAnimatedWebP(signature)
                read >= 8 && signature.startsWith(PNG_SIGNATURE) -> isAnimatedPng(stream)
                else -> false
            }
        }.getOrDefault(false)
    }

    /**
     * A WebP file is only animated if its first (and only top-level) chunk is the extended format header `VP8X`
     * with the animation flag set. Simple WebP files - whose first chunk is `VP8 ` (lossy) or `VP8L` (lossless) -
     * are always static.
     */
    private fun isAnimatedWebP(header: ByteArray): Boolean {
        if (!header.regionMatches(RIFF.size + 4 + WEBP.size, VP8X)) return false
        val flags = header[RIFF.size + 4 + WEBP.size + 4 + 4].toInt()
        return flags and VP8X_ANIMATION_FLAG != 0
    }

    /**
     * Walks the PNG chunk list looking for an `acTL` (animation control) chunk before the first `IDAT` (image
     * data) chunk. Per the APNG spec, this is what distinguishes an animated PNG from a plain one.
     */
    private fun isAnimatedPng(stream: InputStream): Boolean {
        if (!stream.skipFully(PNG_SIGNATURE.size.toLong())) return false
        while (true) {
            val chunkHeader = ByteArray(8)
            if (stream.readAtMost(chunkHeader) < 8) return false
            val length = chunkHeader.beIntAt(0)
            val type = chunkHeader.copyOfRange(4, 8)
            when {
                type.contentEquals(ACTL) -> return true
                type.contentEquals(IDAT) -> return false
                else -> if (!stream.skipFully(length.toLong() + PNG_CRC_SIZE)) return false
            }
        }
    }

    /**
     * A GIF is animated if it contains more than one image descriptor block.
     */
    private fun isAnimatedGif(stream: InputStream): Boolean {
        if (!stream.skipFully(GIF_87A.size.toLong())) return false
        // Logical Screen Descriptor: width(2) + height(2) + packed fields(1) + background color index(1) + pixel aspect ratio(1)
        val screenDescriptor = ByteArray(7)
        if (stream.readAtMost(screenDescriptor) < 7) return false
        if (!stream.skipGifColorTable(screenDescriptor[4])) return false

        var imageCount = 0
        while (true) {
            when (stream.read()) {
                -1 -> return false
                GIF_EXTENSION_INTRODUCER -> {
                    if (stream.read() == -1) return false // Extension label
                    if (!stream.skipGifSubBlocks()) return false
                }
                GIF_IMAGE_SEPARATOR -> {
                    imageCount++
                    if (imageCount > 1) return true
                    // Image Descriptor: left(2) + top(2) + width(2) + height(2) + packed fields(1)
                    val imageDescriptor = ByteArray(9)
                    if (stream.readAtMost(imageDescriptor) < 9) return false
                    if (!stream.skipGifColorTable(imageDescriptor[8])) return false
                    if (stream.read() == -1) return false // LZW minimum code size
                    if (!stream.skipGifSubBlocks()) return false
                }
                GIF_TRAILER -> return false
                else -> return false
            }
        }
    }

    /** Skips a GIF color table if the [packedFields] byte declares one is present. */
    private fun InputStream.skipGifColorTable(packedFields: Byte): Boolean {
        val hasColorTable = packedFields.toInt() and 0x80 != 0
        if (!hasColorTable) return true
        val tableSize = 3 * (1 shl ((packedFields.toInt() and 0x07) + 1))
        return skipFully(tableSize.toLong())
    }

    /** Skips a sequence of length-prefixed GIF sub-blocks, terminated by a zero-length block. */
    private fun InputStream.skipGifSubBlocks(): Boolean {
        while (true) {
            val size = read()
            if (size == -1) return false
            if (size == 0) return true
            if (!skipFully(size.toLong())) return false
        }
    }

    private companion object {
        // 21 bytes is enough to identify every format handled here, and to read a WebP VP8X chunk (including its
        // animation flag) in one go: RIFF(4) + size(4) + WEBP(4) + chunk id(4) + chunk size(4) + flags(1).
        const val SIGNATURE_SIZE = 21

        val GIF_87A = "GIF87a".toByteArray(Charsets.US_ASCII)
        val GIF_89A = "GIF89a".toByteArray(Charsets.US_ASCII)
        val RIFF = "RIFF".toByteArray(Charsets.US_ASCII)
        val WEBP = "WEBP".toByteArray(Charsets.US_ASCII)
        val VP8X = "VP8X".toByteArray(Charsets.US_ASCII)
        val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        val ACTL = "acTL".toByteArray(Charsets.US_ASCII)
        val IDAT = "IDAT".toByteArray(Charsets.US_ASCII)

        const val VP8X_ANIMATION_FLAG = 0x02
        const val PNG_CRC_SIZE = 4L

        const val GIF_EXTENSION_INTRODUCER = 0x21
        const val GIF_IMAGE_SEPARATOR = 0x2C
        const val GIF_TRAILER = 0x3B
    }
}

/** Reads up to [buffer]'s size bytes, looping until either the buffer is full or the stream is exhausted. */
private fun InputStream.readAtMost(buffer: ByteArray): Int {
    var total = 0
    while (total < buffer.size) {
        val read = read(buffer, total, buffer.size - total)
        if (read == -1) break
        total += read
    }
    return total
}

/** Skips exactly [byteCount] bytes, returning `false` if the stream ends before that many bytes are available. */
private fun InputStream.skipFully(byteCount: Long): Boolean {
    var remaining = byteCount
    while (remaining > 0) {
        val skipped = skip(remaining)
        if (skipped <= 0) {
            // `skip` may legitimately return 0 without having reached EOF for some stream implementations, so fall
            // back to reading a single byte to make progress and detect EOF unambiguously.
            if (read() == -1) return false
            remaining -= 1
        } else {
            remaining -= skipped
        }
    }
    return true
}

private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (size < prefix.size) return false
    for (i in prefix.indices) {
        if (this[i] != prefix[i]) return false
    }
    return true
}

private fun ByteArray.regionMatches(offset: Int, other: ByteArray): Boolean {
    if (offset < 0 || offset + other.size > size) return false
    for (i in other.indices) {
        if (this[offset + i] != other[i]) return false
    }
    return true
}

private fun ByteArray.beIntAt(offset: Int): Int {
    return ((this[offset].toInt() and 0xFF) shl 24) or
        ((this[offset + 1].toInt() and 0xFF) shl 16) or
        ((this[offset + 2].toInt() and 0xFF) shl 8) or
        (this[offset + 3].toInt() and 0xFF)
}
