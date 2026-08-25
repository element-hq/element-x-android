/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.linknewdevice

/**
 * Lets the user confirm the two-digit check code displayed by the device being linked, which proves the channel is not being intercepted.
 */
interface CheckCodeSender {
    /**
     * Validates the given [code]. Returns true if the code is valid, false otherwise.
     * This method can be called multiple times to validate different codes.
     */
    suspend fun validate(code: UByte): Boolean

    /**
     * Sends the given [code].
     * This method can be called only once.
     */
    suspend fun send(code: UByte): Result<Unit>
}
