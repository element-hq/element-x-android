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

class RawX509SignerWrapperTest {
    @Test
    fun `sign calls the sign method and maps the result`() = runTest {
        val message = "message".toByteArray()
        val signatureBytes = "signature".toByteArray()
        val certificateChain = "certificate chain"
        val resultSignature = RawX509Signature(
            certificateChain = certificateChain,
            signatureBytes = signatureBytes,
            signatureScheme = X509SignatureScheme.RSA_PSS_SHA512,
        )
        val expectedRustSignature = RustRawX509Signature(
            signatureBytes = signatureBytes,
            certificateChain = certificateChain,
            signatureScheme = RustX509SignatureScheme.RSA_PSS_SHA512,
        )

        val signLambda = lambdaRecorder<ByteArray, RawX509Signature> { _ -> resultSignature }
        val wrapper = RawX509SignerWrapper(rawX509Signer = { m -> signLambda(m) })
        val result = wrapper.sign(message)

        assertThat(result).isEqualTo(expectedRustSignature)
        signLambda.assertions().isCalledOnce().with(value(message))
    }
}
