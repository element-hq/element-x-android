/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.crash

import kotlinx.coroutines.flow.Flow

interface CrashDataStore {
    fun setCrashData(crashData: String)

    suspend fun resetAppHasCrashed()
    fun appHasCrashed(): Flow<Boolean>
    fun crashInfo(): Flow<String>

    suspend fun reset()
}
