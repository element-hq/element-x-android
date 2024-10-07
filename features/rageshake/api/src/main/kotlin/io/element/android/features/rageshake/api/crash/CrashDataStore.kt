/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.api.crash

import kotlinx.coroutines.flow.Flow

interface CrashDataStore {
    fun setCrashData(crashData: String)

    suspend fun resetAppHasCrashed()
    fun appHasCrashed(): Flow<Boolean>
    fun crashInfo(): Flow<String>

    suspend fun reset()
}
