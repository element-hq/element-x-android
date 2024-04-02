/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.permalink

import android.net.Uri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.MatrixConfiguration
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.permalink.MatrixToConverter
import javax.inject.Inject

/**
 * Mapping of an input URI to a matrix.to compliant URI.
 */
@ContributesBinding(AppScope::class)
class DefaultMatrixToConverter @Inject constructor() : MatrixToConverter {
    /**
     * Try to convert a URL from an element web instance or from a client permalink to a matrix.to url.
     * To be successfully converted, URL path should contain one of the [SUPPORTED_PATHS].
     * Examples:
     * - https://riot.im/develop/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     * - https://app.element.io/#/room/#element-android:matrix.org   ->  https://matrix.to/#/#element-android:matrix.org
     * - https://www.example.org/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     */
    override fun convert(uri: Uri): Uri? {
        val uriString = uri.toString()
        val baseUrl = MatrixConfiguration.MATRIX_TO_PERMALINK_BASE_URL

        return when {
            // URL is already a matrix.to
            uriString.startsWith(baseUrl) -> uri
            // Web or client url
            SUPPORTED_PATHS.any { it in uriString } -> {
                val path = SUPPORTED_PATHS.first { it in uriString }
                Uri.parse(baseUrl + uriString.substringAfter(path))
            }
            // URL is not supported
            else -> null
        }
    }

    private val SUPPORTED_PATHS = listOf(
        "/#/room/",
        "/#/user/",
        "/#/group/"
    )
}
