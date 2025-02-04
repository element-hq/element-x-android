/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.mxc

import javax.inject.Inject

class MxcTools @Inject constructor() {
    /**
     * Regex to match a Matrix Content (mxc://) URI.
     *
     * See: https://spec.matrix.org/v1.8/client-server-api/#matrix-content-mxc-uris
     */
    private val mxcRegex = Regex("""^mxc://([^/]+)/([^/]+)$""")

    /**
     * Sanitizes an mxcUri to be used as a relative file path.
     *
     * @param mxcUri the Matrix Content (mxc://) URI of the file.
     * @return the relative file path as "<server-name>/<media-id>" or null if the mxcUri is invalid.
     */
    fun mxcUri2FilePath(mxcUri: String): String? = mxcRegex.matchEntire(mxcUri)?.let { match ->
        buildString {
            append(match.groupValues[1])
            append("/")
            append(match.groupValues[2])
        }
    }
}
