/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import kotlinx.coroutines.flow.Flow

fun interface CanIncludeLogsProvider {
    /**
     * Logs are shared by all the signed-in accounts, so they can be included in a bug report
     * only if all the accounts send their bug reports to the same server.
     */
    fun canIncludeLogs(): Flow<Boolean>
}
