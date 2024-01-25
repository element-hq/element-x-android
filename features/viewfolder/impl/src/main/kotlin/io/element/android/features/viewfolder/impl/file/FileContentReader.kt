/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.viewfolder.impl.file

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

interface FileContentReader {
    suspend fun getLines(path: String): Result<List<String>>
}

@ContributesBinding(AppScope::class)
class DefaultFileContentReader @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
) : FileContentReader {
    override suspend fun getLines(path: String): Result<List<String>> = withContext(dispatchers.io) {
        runCatching {
            File(path).readLines()
        }
    }
}
