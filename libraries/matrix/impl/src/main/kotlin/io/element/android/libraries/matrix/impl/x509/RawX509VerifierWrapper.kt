/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import io.element.android.libraries.matrix.api.x509.RawX509Verifier
import uniffi.matrix_sdk_crypto.RawX509Signature
import uniffi.matrix_sdk_crypto.X509SignatureScheme

/** A shim which maps from our own [RawX509Verifier] interface to the rust-sdk's interface of the same name. */
class RawX509VerifierWrapper(private val rawX509Verifier: RawX509Verifier) : org.matrix.rustcomponents.sdk.RawX509Verifier {
    override fun verify(message: ByteArray, sig: RawX509Signature): Boolean {
        val signatureScheme = when (sig.signatureScheme) {
            X509SignatureScheme.RSA_PSS_SHA512 -> io.element.android.libraries.matrix.api.x509.X509SignatureScheme.RSA_PSS_SHA512
        }

        val sig = io.element.android.libraries.matrix.api.x509.RawX509Signature(
            sig.certificateChain,
            sig.signatureBytes,
            signatureScheme
        )
        return rawX509Verifier.verify(message, sig)
    }
}
