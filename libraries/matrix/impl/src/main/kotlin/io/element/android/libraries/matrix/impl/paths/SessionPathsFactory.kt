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

package io.element.android.libraries.matrix.impl.paths

import io.element.android.libraries.di.CacheDirectory
import java.io.File
import java.util.UUID
import javax.inject.Inject

class SessionPathsFactory @Inject constructor(
    private val baseDirectory: File,
    @CacheDirectory private val cacheDirectory: File,
) {
    fun create(): SessionPaths {
        val subPath = UUID.randomUUID().toString()
        return SessionPaths(
            fileDirectory = File(baseDirectory, subPath),
            cacheDirectory = File(cacheDirectory, subPath),
        )
    }
}
