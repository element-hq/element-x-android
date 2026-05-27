/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.keys

import io.element.android.libraries.matrix.impl.ClientSecret

private const val PASSPHRASE_SIZE = 256
private const val KEY_SIZE = 32

interface SecretGenerator {
    /**
     * Generate a passphrase to encrypt the databases.
     * @param size the size of the passphrase in bytes, before encoding. The default value is 256 bytes.
     * @return either a random passphrase or `null` to not encrypt the databases.
     */
    fun generatePassphrase(size: Int = PASSPHRASE_SIZE): ClientSecret.Passphrase?

    /**
     * Generate a key to encrypt the databases.
     * @param size the size of the key in bytes. The default value is 32 bytes.
     * @return a random key.
     */
    fun generateKey(size: Int = KEY_SIZE): ClientSecret.RawKey
}
