/*
 * Copyright (c) 2023 New Vector Ltd
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
