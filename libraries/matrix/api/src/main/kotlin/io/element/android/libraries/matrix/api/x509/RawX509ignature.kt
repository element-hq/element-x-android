/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.x509

/**
 * The object we receive from [`RawX509Signer`], and pass to
 * [`RawX509Verifier`].
 *
 * A simplified representation of the data in the signature object.
 */
data class RawX509Signature(
    /**
     * The PEM-encoded certificate chain, starting with the device's own
     * certificate, followed by intermediate certificates.
     */
    val certificateChain: String,

    /** The raw bytes of the signature. */
    val signatureBytes: ByteArray,

    /** The algorithm that the signer used to construct the signature. */
    val signatureScheme: X509SignatureScheme,
)

enum class X509SignatureScheme {
    /**
     * SHA-512 message digest, with RSASSA-PSS signature scheme (aka RSA-PSS),
     * per [RFC 8017 § 8.1](https://www.rfc-editor.org/info/rfc8017/#section-8.1).
     *
     * Not to be confused with RSA-PKCS1, which is incompatible.
     */
    RSA_PSS_SHA512,
}
