/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.x509

/**
 * A type which can verify an X.509 signature to a Matrix object.
 *
 * An instance of one of these can be returned by [X509Provider.getRawX509Verifier].
 *
 * @see RawX509Signer
 */
fun interface RawX509Verifier {
    /** Check that `sig` is a valid signature of `message`.
     *
     * Implementations must check:
     *  * The certificate chain is issued via a trusted CA.
     *  * The certificate is valid for today's date.
     *  * The signature itself is a valid signature of `message`.
     *
     * @return `true` if the signature is valid, otherwise `false`.
     */
    fun verify(message: ByteArray, sig: RawX509Signature): Boolean
}
