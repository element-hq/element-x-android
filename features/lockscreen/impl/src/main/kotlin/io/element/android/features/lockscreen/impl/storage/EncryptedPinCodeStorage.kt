/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.storage

import kotlinx.coroutines.flow.Flow

/**
 * Should be implemented by any class that provides access to the encrypted PIN code.
 * All methods are suspending in case there are async IO operations involved.
 */
interface EncryptedPinCodeStorage {
    /**
     * Returns the encrypted PIN code.
     */
    suspend fun getEncryptedCode(): String?

    /**
     * Saves the encrypted PIN code to some persistable storage.
     */
    suspend fun saveEncryptedPinCode(pinCode: String)

    /**
     * Deletes the PIN code from some persistable storage.
     */
    suspend fun deleteEncryptedPinCode()

    /**
     * Returns whether the PIN code is stored or not.
     */
    fun hasPinCode(): Flow<Boolean>
}
