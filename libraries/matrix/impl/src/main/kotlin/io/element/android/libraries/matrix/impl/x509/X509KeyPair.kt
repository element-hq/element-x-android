/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import io.element.android.libraries.matrix.api.x509.RawX509Signature
import io.element.android.libraries.matrix.api.x509.X509Sign
import io.element.android.libraries.matrix.api.x509.X509SignatureScheme
import timber.log.Timber
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.X509Certificate

/** An implementation of X509Sign using key and cert chain from the Android KeyStore */
class X509KeyPair(private val key: PrivateKey, private val certificateChain: Array<out X509Certificate?>) : X509Sign {
    override fun sign(message: ByteArray): RawX509Signature {
        val certificateChainBuilder = StringBuilder()
        for (cert in this.certificateChain) {
            if (cert != null) {
                certificateChainBuilder.append("-----BEGIN CERTIFICATE-----\r\n")
                    .append(certToPem(cert)?.toString(Charsets.US_ASCII))
                    .append("\r\n")
                    .append("-----END CERTIFICATE-----\r\n")
            }
        }

        if (key.algorithm == "RSA") {
            val signature = Signature.getInstance("SHA512withRSA/PSS")
            signature.initSign(this.key)
            signature.update(message)
            Timber.i("X509: signing message %s", String(message))

            return RawX509Signature(
                certificateChain = certificateChainBuilder.toString(),
                signatureScheme = X509SignatureScheme.RSA_PSS_SHA512,
                signatureBytes = signature.sign(),
            )
        } else {
            error("X509: Unable to sign object: unsupported key algorithm "+ key.algorithm)
        }
    }

    private fun certToPem(certificate: Certificate): ByteArray? {
        return java.util.Base64.getMimeEncoder(64, "\r\n".toByteArray()).encode(certificate.encoded)
    }
}
