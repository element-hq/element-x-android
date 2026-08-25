/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.util

import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.AsyncImagePainter
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.exception.ContentScannerErrorReason
import io.element.android.libraries.matrix.api.exception.ContentScannerErrorReason.MCS_MEDIA_NOT_CLEAN
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.mockk.mockk
import org.junit.Test
import java.io.FileNotFoundException

class AsyncImageStateHandlerTest {
    @Test
    fun `test Empty state sets the content validation state to Unknown`() {
        val updateContentValidationState = lambdaRecorder<ContentValidationValue, Unit> {}
        handleAsyncImageStateChange(
            state = AsyncImagePainter.State.Empty,
            onLoaded = {},
            updateContentValidationState = updateContentValidationState,
        )

        updateContentValidationState.assertions().isCalledOnce().with(value(ContentValidationValue.Unknown))
    }

    @Test
    fun `test Loading state sets the content validation state to Loading`() {
        val updateContentValidationState = lambdaRecorder<ContentValidationValue, Unit> {}
        handleAsyncImageStateChange(
            state = AsyncImagePainter.State.Loading(null),
            onLoaded = {},
            updateContentValidationState = updateContentValidationState,
        )

        updateContentValidationState.assertions().isCalledOnce().with(value(ContentValidationValue.Loading))
    }

    @Test
    fun `test Success state sets the content validation state to Valid and triggers onLoaded callback`() {
        val updateContentValidationState = lambdaRecorder<ContentValidationValue, Unit> {}
        val onLoaded = lambdaRecorder<Unit> {}
        handleAsyncImageStateChange(
            state = AsyncImagePainter.State.Success(mockk<Painter>(), SuccessResult(mockk(), mockk())),
            onLoaded = onLoaded,
            updateContentValidationState = updateContentValidationState,
        )

        onLoaded.assertions().isCalledOnce()
        updateContentValidationState.assertions().isCalledOnce().with(value(ContentValidationValue.Valid))
    }

    @Test
    fun `test Failure state sets the content validation state to Invalid if the error matches`() {
        val updateContentValidationState = lambdaRecorder<ContentValidationValue, Unit> {}
        val onLoaded = lambdaRecorder<Unit> {}
        val mediaNotCleanError = ClientException.ContentScanner("dangerous", MCS_MEDIA_NOT_CLEAN)
        val forbiddenMimeTypeError = ClientException.ContentScanner("dangerous", ContentScannerErrorReason.MCS_MIME_TYPE_FORBIDDEN)
        handleAsyncImageStateChange(
            state = AsyncImagePainter.State.Error(null, ErrorResult(null, mockk(), mediaNotCleanError)),
            onLoaded = onLoaded,
            updateContentValidationState = updateContentValidationState,
        )

        handleAsyncImageStateChange(
            state = AsyncImagePainter.State.Error(null, ErrorResult(null, mockk(), forbiddenMimeTypeError)),
            onLoaded = onLoaded,
            updateContentValidationState = updateContentValidationState,
        )

        onLoaded.assertions().isNeverCalled()
        updateContentValidationState.assertions()
            .isCalledExactly(2)
            .withSequence(
                listOf(value(ContentValidationValue.Invalid)),
                listOf(value(ContentValidationValue.Invalid))
            )
    }

    @Test
    fun `test Failure state sets the content validation state to Uknown if the error does not match a content scanner one`() {
        val updateContentValidationState = lambdaRecorder<ContentValidationValue, Unit> {}
        val onLoaded = lambdaRecorder<Unit> {}
        val error = FileNotFoundException("File not found")
        handleAsyncImageStateChange(
            state = AsyncImagePainter.State.Error(null, ErrorResult(null, mockk(), error)),
            onLoaded = onLoaded,
            updateContentValidationState = updateContentValidationState,
        )

        onLoaded.assertions().isNeverCalled()
        updateContentValidationState.assertions().isCalledOnce().with(value(ContentValidationValue.Unknown))
    }
}
