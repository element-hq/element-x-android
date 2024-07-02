/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
