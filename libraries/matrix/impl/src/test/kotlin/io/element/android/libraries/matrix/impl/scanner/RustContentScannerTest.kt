/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.scanner

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.test.media.aMediaSource
import kotlinx.coroutines.test.runTest
import org.junit.Test
import uniffi.matrix_sdk_contentscanner.MediaScanResponse

class RustContentScannerTest {
    @Test
    fun `test successful valid scan`() = runTest {
        val scanner = RustContentScanner(
            client = FakeFfiClient(),
            rustScanner = FakeFfiContentScanner(),
        )

        scanner.scan(aMediaSource()).run {
            assertThat(isSuccess).isTrue()
            assertThat(getOrNull()).isTrue()
        }
    }

    @Test
    fun `test successful invalid scan`() = runTest {
        val scanner = RustContentScanner(
            client = FakeFfiClient(),
            rustScanner = FakeFfiContentScanner(scan = { _, _ -> MediaScanResponse(clean = false, info = "Not clean") })
        )

        scanner.scan(aMediaSource()).run {
            assertThat(isSuccess).isTrue()
            assertThat(getOrNull()).isFalse()
        }
    }

    @Test
    fun `test failed scan`() = runTest {
        val scanner = RustContentScanner(
            client = FakeFfiClient(),
            rustScanner = FakeFfiContentScanner(scan = { _, _ -> throw IllegalStateException("BOOM") })
        )

        scanner.scan(aMediaSource()).run {
            assertThat(isFailure).isTrue()
            assertThat(getOrNull()).isNull()
        }
    }
}
