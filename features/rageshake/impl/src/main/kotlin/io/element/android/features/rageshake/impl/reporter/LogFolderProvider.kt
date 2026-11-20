/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import kotlinx.coroutines.flow.Flow

fun interface LogFolderProvider {
    /**
     * Provide the folder where the log files have to be written, which depends on the bug report configuration
     * of all the signed-in accounts.
     */
    fun logFolderFlow(): Flow<LogFolder>
}
