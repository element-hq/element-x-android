/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.crypto

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.InputStream

/**
 * This class is used to read encrypted data from a file.
 *
 * It comes directly from [androidx.security.crypto](https://developer.android.com/reference/androidx/security/crypto/package-summary).
 */
internal class EncryptedFileInputStream(
    fileDescriptor: FileDescriptor,
    private val inputStream: InputStream,
) : FileInputStream(fileDescriptor) {
    private val lock = Any()

    override fun read(): Int = inputStream.read()

    override fun read(b: ByteArray?): Int = inputStream.read(b)

    override fun read(b: ByteArray?, off: Int, len: Int): Int = inputStream.read(b, off, len)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun readAllBytes(): ByteArray? = inputStream.readAllBytes()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun readNBytes(b: ByteArray?, off: Int, len: Int): Int = inputStream.readNBytes(b, off, len)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun readNBytes(len: Int): ByteArray? = inputStream.readNBytes(len)

    override fun skip(n: Long): Long = inputStream.skip(n)

    override fun available(): Int = inputStream.available()

    override fun mark(readlimit: Int) = synchronized(lock) { inputStream.mark(readlimit) }

    override fun markSupported(): Boolean = inputStream.markSupported()

    override fun reset() = synchronized(lock) { inputStream.reset() }

    override fun close() = inputStream.close()
}
