/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.call

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.call.ElementCallBaseUrlProvider
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultElementCallBaseUrlProvider @Inject constructor(
    private val elementWellKnownParser: ElementWellKnownParser,
) : ElementCallBaseUrlProvider {
    override suspend fun provides(matrixClient: MatrixClient): String? {
        val url = buildString {
            append("https://")
            append(matrixClient.userIdServerName())
            append("/.well-known/element/element.json")
        }
        return matrixClient.getUrl(url)
            .onFailure { failure ->
                Timber.w(failure, "Failed to fetch well-known element.json")
            }
            .getOrNull()
            ?.let { wellKnownStr ->
                elementWellKnownParser.parse(wellKnownStr)
                    .onFailure { failure ->
                        // Can be a HTML 404.
                        Timber.w(failure, "Failed to parse content")
                    }
                    .getOrNull()
            }
            ?.call
            ?.widgetUrl
    }
}
