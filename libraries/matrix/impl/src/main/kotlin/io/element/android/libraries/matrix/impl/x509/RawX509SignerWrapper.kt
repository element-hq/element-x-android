/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import io.element.android.libraries.matrix.api.x509.RawX509Signer
import io.element.android.libraries.matrix.api.x509.X509SignatureScheme
import uniffi.matrix_sdk_crypto.RawX509Signature

/** A shim which maps from our own [RawX509Signer] interface to the rust-sdk's interface of the same name */
class RawX509SignerWrapper(private val rawX509Signer: RawX509Signer): org.matrix.rustcomponents.sdk.RawX509Signer {
    override fun sign(message: ByteArray): RawX509Signature {
        val sig = rawX509Signer.sign(message)
        val signatureScheme = when(sig.signatureScheme) {
            X509SignatureScheme.RSA_PSS_SHA512 -> uniffi.matrix_sdk_crypto.X509SignatureScheme.RSA_PSS_SHA512
        };
        return RawX509Signature(sig.signatureBytes, sig.certificateChain, signatureScheme)
    }
}
