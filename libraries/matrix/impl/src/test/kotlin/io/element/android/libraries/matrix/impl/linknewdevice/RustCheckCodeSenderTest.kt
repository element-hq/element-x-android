/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.linknewdevice

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiCheckCodeSender
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustCheckCodeSenderTest {
    @Test
    fun `send invokes the Ffi object`() = runTest {
        val sendResult = lambdaRecorder<UByte, Unit> { }
        val sut = RustCheckCodeSender(
            inner = FakeFfiCheckCodeSender(
                sendResult = sendResult,
            ),
            sessionDispatcher = StandardTestDispatcher(testScheduler),
        )
        sut.send(1.toUByte())
        sendResult.assertions().isCalledOnce().with(value(1.toUByte()))
    }

    @Test
    fun `validate always returns true for now`() = runTest {
        val sut = RustCheckCodeSender(
            inner = FakeFfiCheckCodeSender(),
            sessionDispatcher = StandardTestDispatcher(testScheduler),
        )
        val result = sut.validate(1.toUByte())
        assertThat(result).isTrue()
    }
}
