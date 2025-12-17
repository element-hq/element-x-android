/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cryptography.api

import javax.crypto.SecretKey

/**
 * Simple interface to get, create and delete a secret key for a given alias.
 * Implementation should be able to store the generated key securely.
 */
interface SecretKeyRepository {
    /**
     * Get or create a secret key for a given alias.
     * @param alias the alias to use
     * @param requiresUserAuthentication true if the key should be protected by user authentication
     */
    fun getOrCreateKey(alias: String, requiresUserAuthentication: Boolean): SecretKey

    /**
     * Delete the secret key for a given alias.
     * @param alias the alias to use
     */
    fun deleteKey(alias: String)
}
