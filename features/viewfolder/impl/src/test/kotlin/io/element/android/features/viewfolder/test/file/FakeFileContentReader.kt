/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.viewfolder.test.file

import io.element.android.features.viewfolder.impl.file.FileContentReader

class FakeFileContentReader : FileContentReader {
    private var result: Result<List<String>> = Result.success(emptyList())

    fun givenResult(result: Result<List<String>>) {
        this.result = result
    }

    override suspend fun getLines(path: String): Result<List<String>> = result
}
