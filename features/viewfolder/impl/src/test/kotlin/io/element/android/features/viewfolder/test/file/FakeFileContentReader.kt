/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
