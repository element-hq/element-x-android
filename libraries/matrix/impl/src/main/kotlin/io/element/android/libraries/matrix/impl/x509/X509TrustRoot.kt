/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import android.util.Base64

import org.matrix.rustcomponents.sdk.X509Signature
import org.matrix.rustcomponents.sdk.X509Verify
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.Signature
import java.security.cert.CertPath
import java.security.cert.CertPathValidator
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.PKIXCertPathValidatorResult
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate



class X509TrustRoot : X509Verify {
    override fun verify(message: ByteArray, sig: X509Signature): Boolean {
        Timber.i("X509TrustRoot.verify()")
        try {
            if (sig.signatureScheme != 0x0806.toUShort()) {
                Timber.i("X509: Unsupported signature scheme %#x", sig.signatureScheme)
                return false
            }

            // Parse the certs into a cert path
            val cf = CertificateFactory.getInstance("X.509")
            val certStream = ByteArrayInputStream(sig.certificateChain.toByteArray(StandardCharsets.UTF_8))
            val certs = cf.generateCertificates(certStream)
            val certPath = cf.generateCertPath(ArrayList<Certificate?>(certs))

            /* Step 1: Verify the certificate chain */
            validateCertPath(certPath)
            Timber.i("X509: cert path verified")

            /* Step 2: Check the signature on the signed object */
            val leafCert = certPath.certificates[0]
            var signature = Signature.getInstance("SHA256withRSA/PSS");
            signature.initVerify(leafCert.publicKey);
            signature.update(message);
            if (signature.verify(Base64.decode(sig.signature, Base64.NO_PADDING))) {
                Timber.i("X509: signature verified")
            } else {
                Timber.w("X509: signature invalid. Message was: %s", String(message))
                return false
            }

            return true
        } catch ( e: Exception) {
            Timber.e(e,"X509: Error during verification")
            return false
        }
    }

    private fun validateCertPath(certPath: CertPath) {
        // Set up our trust anchors, based on certificates in the store
        val keystore = KeyStore.getInstance("AndroidCAStore")
        keystore.load(null)
        val anchors = HashSet<TrustAnchor?>()
        val aliases = keystore.aliases()
        while (aliases.hasMoreElements()) {
            val alias = aliases.nextElement()
            val cert = keystore.getCertificate(alias)
            if (cert is X509Certificate) {
                anchors.add(TrustAnchor(cert, null))
            }
        }

        // Validate the cert path
        val params = PKIXParameters(anchors)
        params.isRevocationEnabled = false // TODO
        val validator = CertPathValidator.getInstance("PKIX")
        val result = validator.validate(certPath, params) as PKIXCertPathValidatorResult

        Timber.i("X509: Validated certPath via Trust anchor %s", result.trustAnchor.trustedCert.subjectDN)
    }

}
