/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import io.element.android.libraries.matrix.api.x509.X509Sign
import io.element.android.libraries.matrix.api.x509.X509SignatureScheme
import uniffi.matrix_sdk_crypto.RawX509Signature

/** A shim which maps from our own [X509Sign] interface to the rust-sdk's interface of the same name */
class X509SignWrapper(private val x509Sign: X509Sign) : org.matrix.rustcomponents.sdk.X509Sign {
    override fun sign(message: ByteArray): RawX509Signature {
        val sig = x509Sign.sign(message)
        val signatureScheme = when (sig.signatureScheme) {
            X509SignatureScheme.RSA_PSS_SHA512 -> uniffi.matrix_sdk_crypto.X509SignatureScheme.RSA_PSS_SHA512
        }
        return RawX509Signature(sig.signatureBytes, sig.certificateChain, signatureScheme)
    }
}
