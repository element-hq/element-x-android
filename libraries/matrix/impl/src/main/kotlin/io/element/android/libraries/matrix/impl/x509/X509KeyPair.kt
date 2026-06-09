/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import android.util.Base64
import org.matrix.rustcomponents.sdk.X509Sign
import org.matrix.rustcomponents.sdk.X509Signature
import org.matrix.rustcomponents.sdk.X509SignatureAndKeyId
import timber.log.Timber
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.X509Certificate

/** An implementation of X509Sign using key and cert chain from the Android KeyStore */
class X509KeyPair(private val key: PrivateKey, private val certificateChain: Array<out X509Certificate?>) : X509Sign {
    private val deviceId: String;

    init {
        val lastCert = this.certificateChain[this.certificateChain.size - 1] ?: error("X509: empty certificate chain")

        val aki = lastCert.getExtensionValue("2.5.29.35") // AuthorityKeyIdentifier

        // TODO: consider using BouncyCastle or something to parse this properly
        // First six bytes should be:
        //   04 - OCTET STRING
        //     18 - Length (24 bytes)
        //     30 - SEQUENCE tag
        //       16 - Length (22 bytes)
        //       80 - keyIdentifier tag
        //         14 - Length (20 bytes)
        if (aki.size < 6
            || aki[0] != 0x04.toByte()
            || aki[1] != (aki.size - 2).toByte()
            || aki[2] != 0x30.toByte()
            || aki[3] != (aki.size - 4).toByte()
            || aki[4] != 0x80.toByte()
            || aki[5] != (aki.size - 6).toByte()
        ) {
            error("X509: unable to read AuthorityKeyIdentifier from certificate chain")
        }

        this.deviceId = Base64.encodeToString(aki.sliceArray(6..aki.size - 1), Base64.NO_WRAP + Base64.NO_PADDING)
    }

    override fun sign(message: ByteArray): X509SignatureAndKeyId {
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
            val signature = Signature.getInstance("SHA256withRSA/PSS");
            signature.initSign(this.key)
            signature.update(message)
            Timber.i("X509: signing message %s", String(message))

            val x509Signature = X509Signature(
                certificateChain = certificateChainBuilder.toString(),
                signatureScheme = 0x0806u, // https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#tls-signaturescheme
                signature = Base64.encodeToString(signature.sign(), Base64.NO_PADDING)
            )

            return X509SignatureAndKeyId(deviceId = deviceId, signature = x509Signature)
        } else {
            error("X509: Unable to sign object: unsupported key algorithm "+ key.algorithm)
        }
    }

    private fun certToPem(certificate: Certificate): ByteArray? {
        return java.util.Base64.getMimeEncoder(64, "\r\n".toByteArray()).encode(certificate.encoded)
    }
}
