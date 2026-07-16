/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl.di

import com.google.common.truth.Truth.assertThat
import io.element.android.features.contentscanner.impl.DefaultContentScannerService
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import io.element.android.libraries.matrix.test.scanner.FakeContentScanner
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ContentScannerModuleTest {
    @Test
    fun `ContentScannerModule provides NoopContentScannerService when ContentScanner is null`() = runTest {
        // Given
        val contentScanner: ContentScanner? = null
        val sessionCoroutineScope = backgroundScope
        val coroutineDispatchers = testCoroutineDispatchers()

        // When
        val contentScannerService = ContentScannerModule.providesContentScannerService(
            contentScanner = contentScanner,
            coroutineScope = sessionCoroutineScope,
            coroutineDispatchers = coroutineDispatchers,
        )

        // Then
        assertThat(contentScannerService).isInstanceOf(AlwaysValidContentScannerService::class.java)
    }

    @Test
    fun `ContentScannerModule provides DefaultContentScannerService when ContentScanner is not null`() = runTest {
        // Given
        val contentScanner: ContentScanner? = FakeContentScanner()
        val sessionCoroutineScope = backgroundScope
        val coroutineDispatchers = testCoroutineDispatchers()

        // When
        val contentScannerService = ContentScannerModule.providesContentScannerService(
            contentScanner = contentScanner,
            coroutineScope = sessionCoroutineScope,
            coroutineDispatchers = coroutineDispatchers,
        )

        // Then
        assertThat(contentScannerService).isInstanceOf(DefaultContentScannerService::class.java)
    }
}
