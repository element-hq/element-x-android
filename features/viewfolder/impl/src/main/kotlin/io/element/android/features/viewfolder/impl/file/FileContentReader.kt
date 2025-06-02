/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
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
        runCatchingExceptions {
            File(path).readLines()
        }
    }
}
