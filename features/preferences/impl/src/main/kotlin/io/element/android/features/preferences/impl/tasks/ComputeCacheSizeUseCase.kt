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

package io.element.android.features.preferences.impl.tasks

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidtools.api.FileSizeFormatter
import io.element.android.libraries.androidutils.file.getSizeOfFiles
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ComputeCacheSizeUseCase {
    suspend fun execute(): String
}

@ContributesBinding(SessionScope::class)
class DefaultComputeCacheSizeUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matrixClient: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val fileSizeFormatter: FileSizeFormatter,
) : ComputeCacheSizeUseCase {
    override suspend fun execute(): String = withContext(coroutineDispatchers.io) {
        var cumulativeSize = 0L
        cumulativeSize += matrixClient.getCacheSize()
        // - 4096 to not include the size fo the folder
        cumulativeSize += (context.cacheDir.getSizeOfFiles() - 4096).coerceAtLeast(0)
        fileSizeFormatter.format(cumulativeSize)
    }
}
