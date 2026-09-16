/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.encrypteddb.passphrase

import io.element.android.libraries.androidutils.crypto.ClientSecret

/**
 * An abstraction to implement secure providers for SQLCipher passphrases.
 */
interface DatabaseSecretProvider {
    /**
     * Returns a secret for SQLCipher.
     */
    fun getSecret(): ClientSecret

    /**
     * Resets the passphrase, for example by deleting the persisted secret. Returns `true` if the reset was successful, `false` otherwise.
     */
    fun reset(): Boolean
}
