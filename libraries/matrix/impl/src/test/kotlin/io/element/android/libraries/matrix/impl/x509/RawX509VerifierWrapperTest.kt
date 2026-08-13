/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.x509.RawX509Signature
import io.element.android.libraries.matrix.api.x509.X509SignatureScheme
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.matrix_sdk_crypto.RawX509Signature as RustRawX509Signature
import uniffi.matrix_sdk_crypto.X509SignatureScheme as RustX509SignatureScheme

class RawX509VerifierWrapperTest {
    @Test
    fun `verify maps the rust signature and forwards the result to the wrapped verifier`() = runTest {
        val message = "message".toByteArray()
        val signatureBytes = "signature".toByteArray()
        val certificateChain = "certificate chain"
        val rustSignature = RustRawX509Signature(
            signatureBytes = signatureBytes,
            certificateChain = certificateChain,
            signatureScheme = RustX509SignatureScheme.RSA_PSS_SHA512,
        )
        val expectedApiSignature = RawX509Signature(
            certificateChain = certificateChain,
            signatureBytes = signatureBytes,
            signatureScheme = X509SignatureScheme.RSA_PSS_SHA512,
        )

        val verifyLambda = lambdaRecorder<ByteArray, RawX509Signature, Boolean> { _, _ -> true }
        val wrapper = RawX509VerifierWrapper(rawX509Verifier = { m, s -> verifyLambda(m, s) })
        val result = wrapper.verify(message, rustSignature)

        assertThat(result).isTrue()
        verifyLambda.assertions().isCalledOnce().with(
            value(message),
            value(expectedApiSignature),
        )
    }

    @Test
    fun `verify returns false when the wrapped verifier rejects the signature`() = runTest {
        val rustSignature = RustRawX509Signature(
            signatureBytes = "signature".toByteArray(),
            certificateChain = "certificate chain",
            signatureScheme = RustX509SignatureScheme.RSA_PSS_SHA512,
        )

        val wrapper = RawX509VerifierWrapper(rawX509Verifier = { _, _ -> false })
        val result = wrapper.verify("message".toByteArray(), rustSignature)

        assertThat(result).isFalse()
    }
}
