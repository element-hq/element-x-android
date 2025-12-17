/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.logs

import java.io.File

interface LogFilesRemover {
    /**
     * Perform the log files removal.
     * @param predicate a predicate to filter the files to remove. By default, all files are removed.
     */
    suspend fun perform(predicate: (File) -> Boolean = { true })
}
