/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.permalink

import android.net.Uri
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.MatrixConfiguration
import io.element.android.libraries.core.extensions.replacePrefix
import io.element.android.libraries.matrix.api.permalink.MatrixToConverter

/**
 * Mapping of an input URI to a matrix.to compliant URI.
 */
@ContributesBinding(AppScope::class)
class DefaultMatrixToConverter : MatrixToConverter {
    /**
     * Try to convert a URL from an element web instance or from a client permalink to a matrix.to url.
     * To be successfully converted, URL path should contain one of the [SUPPORTED_PATHS].
     * Examples:
     * - https://riot.im/develop/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     * - https://app.element.io/#/room/#element-android:matrix.org   ->  https://matrix.to/#/#element-android:matrix.org
     * - https://www.example.org/#/room/#element-android:matrix.org  ->  https://matrix.to/#/#element-android:matrix.org
     * Also convert links coming from the matrix.to website:
     * - element://room/#element-android:matrix.org                  ->  https://matrix.to/#/#element-android:matrix.org
     * - element://user/@alice:matrix.org                            ->  https://matrix.to/#/@alice:matrix.org
     */
    override fun convert(uri: Uri): Uri? {
        val uriString = uri.toString()
            // Handle links coming from the matrix.to website.
            .replacePrefix(MATRIX_TO_CUSTOM_SCHEME_BASE_URL, "https://app.element.io/#/")
        val baseUrl = MatrixConfiguration.MATRIX_TO_PERMALINK_BASE_URL

        return when {
            // URL is already a matrix.to
            uriString.startsWith(baseUrl) -> uri
            // Web or client url
            SUPPORTED_PATHS.any { it in uriString } -> {
                val path = SUPPORTED_PATHS.first { it in uriString }
                (baseUrl + uriString.substringAfter(path)).toUri()
            }
            // URL is not supported
            else -> null
        }
    }

    companion object {
        private const val MATRIX_TO_CUSTOM_SCHEME_BASE_URL = "element://"
        private val SUPPORTED_PATHS = listOf(
            "/#/room/",
            "/#/user/",
            "/#/group/"
        )
    }
}
