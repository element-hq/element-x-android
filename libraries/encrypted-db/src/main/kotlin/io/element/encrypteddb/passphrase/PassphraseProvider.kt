/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.passphrase

/**
 * An abstraction to implement secure providers for SQLCipher passphrases.
 */
interface PassphraseProvider {
    /**
     * Returns a passphrase for SQLCipher in [ByteArray] format.
     */
    fun getPassphrase(): ByteArray
}
