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

package io.element.android.libraries.matrix.api.media

import java.io.Closeable
import java.io.File

/**
 * A wrapper around a media file on the disk.
 * When closed the file will be removed from the disk unless [persist] has been used.
 */
interface MediaFile : Closeable {
    fun path(): String

    /**
     * Persists the temp file to the given path. The file will be moved to
     * the given path and won't be deleted anymore when closing the handle.
     */
    fun persist(path: String): Boolean
}

fun MediaFile.toFile(): File {
    return File(path())
}
