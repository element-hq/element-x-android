/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.test.scanner.FakeContentScanner
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import io.element.android.libraries.matrix.ui.media.contentvalidation.DefaultContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.FileNotFoundException

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultContentScannerServiceTest {
    @Test
    fun `scan with no media sources returns unknown`() = runTest {
        val scanLambda = lambdaRecorder<MediaSource, Result<Boolean>> { Result.success(true) }
        val scanner = FakeContentScanner(scan = scanLambda)
        val contentValidationState = DefaultContentValidationState()
        createDefaultContentScannerService(
            contentScanner = scanner,
        ).scan(
            mediaSources = emptyList(),
            contentValidationState = contentValidationState,
        )

        scanLambda.assertions().isNeverCalled()
        assertThat(contentValidationState.getCurrentOverallState()).isEqualTo(ContentValidationValue.Unknown)
    }

    @Test
    fun `scan with single media source`() = runTest {
        val scanLambda = lambdaRecorder<MediaSource, Result<Boolean>> { Result.success(false) }
        val scanner = FakeContentScanner(scan = scanLambda)
        val contentValidationState = DefaultContentValidationState()

        val mediaSource = aMediaSource("https://example.com/media.jpg")
        createDefaultContentScannerService(
            contentScanner = scanner,
        ).scan(
            mediaSources = listOf(mediaSource),
            contentValidationState = contentValidationState,
        )

        runCurrent()

        scanLambda.assertions().isCalledOnce().with(value(mediaSource))
        assertThat(contentValidationState.getCurrentOverallState()).isEqualTo(ContentValidationValue.Invalid)
    }

    @Test
    fun `scan with several media sources`() = runTest {
        val mediaSourceA = aMediaSource("https://example.com/mediaA.jpg")
        val mediaSourceB = aMediaSource("https://example.com/mediaB.jpg")

        val scanLambda = lambdaRecorder<MediaSource, Result<Boolean>> { source ->
            when (source) {
                mediaSourceA -> Result.success(true)
                mediaSourceB -> Result.success(false)
                else -> error("Unexpected media source: $source")
            }
        }
        val scanner = FakeContentScanner(scan = scanLambda)
        val contentValidationState = DefaultContentValidationState()

        createDefaultContentScannerService(
            contentScanner = scanner,
        ).scan(
            mediaSources = listOf(mediaSourceA, mediaSourceB),
            contentValidationState = contentValidationState,
        )

        runCurrent()

        scanLambda.assertions().isCalledExactly(2)
        assertThat(contentValidationState.getMediaStateFlow(mediaSourceA.safeUrl).first()).isEqualTo(ContentValidationValue.Valid)
        assertThat(contentValidationState.getMediaStateFlow(mediaSourceB.safeUrl).first()).isEqualTo(ContentValidationValue.Invalid)
        assertThat(contentValidationState.overallStateFlow.first()).isEqualTo(ContentValidationValue.Invalid)
    }

    @Test
    fun `scan won't be repeated for sources which are already being scanned`() = runTest {
        val scanLambda = lambdaRecorder<MediaSource, Result<Boolean>> { Result.success(false) }
        val scanner = FakeContentScanner(scan = scanLambda)
        val contentValidationState = DefaultContentValidationState()

        val mediaSource = aMediaSource("https://example.com/media.jpg")
        val service = createDefaultContentScannerService(
            contentScanner = scanner,
        )

        // First scan attempt
        service.scan(
            mediaSources = listOf(mediaSource),
            contentValidationState = contentValidationState,
        )

        // Immediately after, a new scan attempt is made for the same media source
        service.scan(
            mediaSources = listOf(mediaSource),
            contentValidationState = contentValidationState,
        )

        runCurrent()

        // The scan action was done just once, and the overall state is invalid as expected
        scanLambda.assertions().isCalledOnce().with(value(mediaSource))
        assertThat(contentValidationState.getCurrentOverallState()).isEqualTo(ContentValidationValue.Invalid)
    }

    @Test
    fun `scan won't be repeated for sources which already have a cached value`() = runTest {
        val scanLambda = lambdaRecorder<MediaSource, Result<Boolean>> { Result.success(false) }
        val scanner = FakeContentScanner(scan = scanLambda)
        val contentValidationState = NoopContentValidationState(ContentValidationValue.Valid)

        val mediaSource = aMediaSource("https://example.com/media.jpg")
        val service = createDefaultContentScannerService(
            contentScanner = scanner,
        )

        service.scan(
            mediaSources = listOf(mediaSource),
            contentValidationState = contentValidationState,
        )

        runCurrent()

        scanLambda.assertions().isNeverCalled()
    }

    @Test
    fun `if a scan fails with a recoverable error, the content validation value will be back to Unknown`() = runTest {
        val scanLambda = lambdaRecorder<MediaSource, Result<Boolean>> { Result.failure(FileNotFoundException("Some IO exception")) }
        val scanner = FakeContentScanner(scan = scanLambda)
        val contentValidationState = DefaultContentValidationState()

        val mediaSource = aMediaSource("https://example.com/media.jpg")
        val service = createDefaultContentScannerService(
            contentScanner = scanner,
        )

        service.scan(
            mediaSources = listOf(mediaSource),
            contentValidationState = contentValidationState,
        )

        runCurrent()

        scanLambda.assertions().isCalledOnce().with(value(mediaSource))
        assertThat(contentValidationState.getCurrentMediaState(mediaSource.safeUrl)).isEqualTo(ContentValidationValue.Unknown)
        assertThat(contentValidationState.getCurrentOverallState()).isEqualTo(ContentValidationValue.Unknown)
    }

    @Test
    fun `if a scan fails with an unrecoverable error, the content validation value will be back to UnrecoverableError`() = runTest {
        val error = OutOfMemoryError("BOOM")
        val scanLambda = lambdaRecorder<MediaSource, Result<Boolean>> { Result.failure(error) }
        val scanner = FakeContentScanner(scan = scanLambda)
        val contentValidationState = DefaultContentValidationState()

        val mediaSource = aMediaSource("https://example.com/media.jpg")
        val service = createDefaultContentScannerService(
            contentScanner = scanner,
        )

        service.scan(
            mediaSources = listOf(mediaSource),
            contentValidationState = contentValidationState,
        )

        runCurrent()

        scanLambda.assertions().isCalledOnce().with(value(mediaSource))
        assertThat(contentValidationState.getCurrentMediaState(mediaSource.safeUrl)).isEqualTo(ContentValidationValue.UnrecoverableError(error))
        assertThat(contentValidationState.getCurrentOverallState()).isEqualTo(ContentValidationValue.UnrecoverableError(error))
    }

    private fun TestScope.createDefaultContentScannerService(
        contentScanner: ContentScanner = FakeContentScanner(),
        sessionCoroutineScope: CoroutineScope = backgroundScope,
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
    ): DefaultContentScannerService {
        return DefaultContentScannerService(
            contentScanner = contentScanner,
            coroutineScope = sessionCoroutineScope,
            coroutineDispatchers = coroutineDispatchers
        )
    }
}
