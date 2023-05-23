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

package io.element.android.features.messages.impl.media.local

import android.content.ContentResolver
import android.net.Uri
import io.element.android.libraries.androidutils.uri.ASSET_FILE_PATH_ROOT
import io.element.android.libraries.androidutils.uri.firstPathSegment
import java.io.File

/**
 * Tries to convert a URI to a File.
 * Extracted from Coil [coil.map.FileUriMapper]
 */
object UriToFileMapper {

    fun map(data: Uri): File? {
        if (!isApplicable(data)) return null
        return if (data.scheme == ContentResolver.SCHEME_FILE) {
            data.path?.let(::File)
        } else {
            // If the scheme is not "file", it's null, representing a literal path on disk.
            // Assume the entire input, regardless of any reserved characters, is valid.
            File(data.toString())
        }
    }

    private fun isApplicable(data: Uri): Boolean {
        return data.scheme.let { it == null || it == ContentResolver.SCHEME_FILE } &&
            data.path.orEmpty().startsWith('/') && data.firstPathSegment != null
    }

    private fun isAssetUri(uri: Uri): Boolean {
        return uri.scheme == ContentResolver.SCHEME_FILE && uri.firstPathSegment == ASSET_FILE_PATH_ROOT
    }
}
