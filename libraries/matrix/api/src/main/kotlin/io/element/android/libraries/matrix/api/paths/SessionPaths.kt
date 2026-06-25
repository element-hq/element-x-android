/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.paths

import java.io.File

data class SessionPaths(
    val fileDirectory: File,
    val cacheDirectory: File,
) {
    fun deleteRecursively() {
        fileDirectory.deleteRecursively()
        cacheDirectory.deleteRecursively()
    }
}
