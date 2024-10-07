/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
