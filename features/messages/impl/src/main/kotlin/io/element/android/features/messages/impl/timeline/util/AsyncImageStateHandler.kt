/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.util

import coil3.compose.AsyncImagePainter
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import okio.IOException

fun handleAsyncImageStateChange(
    state: AsyncImagePainter.State,
    onLoaded: () -> Unit,
    updateContentValidationState: ((ContentValidationValue) -> Unit)?,
) {
    if (state is AsyncImagePainter.State.Success) {
        onLoaded()
    }

    val updatedContentValidationValue = when (state) {
        is AsyncImagePainter.State.Empty -> ContentValidationValue.Unknown
        is AsyncImagePainter.State.Loading -> ContentValidationValue.Loading
        is AsyncImagePainter.State.Success -> ContentValidationValue.Valid
        is AsyncImagePainter.State.Error -> {
            when (val cause = state.result.throwable) {
                is ClientException.ContentScanner if cause.reason.isDangerous() -> ContentValidationValue.Invalid
                is IOException -> {
                    // Use `Unknown` instead of `Invalid` to allow retrying the download later
                    ContentValidationValue.Unknown
                }
                else -> ContentValidationValue.UnrecoverableError(cause)
            }
        }
    }

    updateContentValidationState?.invoke(updatedContentValidationValue)
}
