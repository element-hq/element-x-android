/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.crypto

import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * This class is used to write encrypted data to a file.
 *
 * It comes directly from [androidx.security.crypto](https://developer.android.com/reference/androidx/security/crypto/package-summary).
 */
internal class EncryptedFileOutputStream(
    fileDescriptor: FileDescriptor,
    private val outputStream: OutputStream
) : FileOutputStream(fileDescriptor) {
    override fun write(b: ByteArray?) = outputStream.write(b)

    override fun write(b: ByteArray?, off: Int, len: Int) = outputStream.write(b, off, len)

    override fun write(b: Int) = outputStream.write(b)

    override fun flush() = outputStream.flush()

    override fun close() = outputStream.close()
}
