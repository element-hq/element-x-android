/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.contentscanner.impl

import io.element.android.features.contentscanner.api.ContentScannerService
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.scanner.ContentScanner
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Default implementation of [ContentScannerService] that uses a [ContentScanner] to scan media sources.
 */
class DefaultContentScannerService(
    private val contentScanner: ContentScanner,
    private val coroutineScope: CoroutineScope,
    coroutineDispatchers: CoroutineDispatchers,
) : ContentScannerService {
    private val dispatcher = coroutineDispatchers.io.limitedParallelism(4)

    override fun scan(mediaSources: List<MediaSource>, contentValidationState: ContentValidationState) {
        for (mediaSource in mediaSources) {
            val url = mediaSource.safeUrl
            val currentState = contentValidationState.getCurrentMediaState(url)
            if (currentState != ContentValidationValue.Unknown) continue
            contentValidationState.update(url, ContentValidationValue.Loading)

            coroutineScope.launch(dispatcher) {
                contentScanner.scan(mediaSource)
                    .onSuccess { isValid ->
                        val contentValidationValue = if (isValid) ContentValidationValue.Valid else ContentValidationValue.Invalid
                        contentValidationState.update(url, contentValidationValue)
                    }
                    .onFailure { exception ->
                        when (exception) {
                            is IOException -> {
                                // If it's an IO-related exception, we can retry later, so we don't store the error
                                contentValidationState.update(url, ContentValidationValue.Unknown)
                            }
                            is ClientException.ContentScanner if exception.reason.isDangerous() -> {
                                // It's a dangerous content (banned mime-type?), we mark it as invalid
                                contentValidationState.update(url, ContentValidationValue.Invalid)
                            }
                            else -> {
                                // For other exceptions, we cache the failure
                                contentValidationState.update(url, ContentValidationValue.UnrecoverableError(exception))
                            }
                        }
                    }
            }
        }
    }
}
