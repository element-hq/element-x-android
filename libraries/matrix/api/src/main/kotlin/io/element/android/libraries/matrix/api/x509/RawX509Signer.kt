/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.x509

/**
 * A type which can add an X.509 signature to a Matrix object.
 *
 * An instance of one of these can be returned by [X509Provider.getRawX509Signer].
 *
 * @see RawX509Verifier
 */
interface RawX509Signer {
    /** Create a signature for the given data, using our private key. */
    fun sign(message: ByteArray): RawX509Signature
}
