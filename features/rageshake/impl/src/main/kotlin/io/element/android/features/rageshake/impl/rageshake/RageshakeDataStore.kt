/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.rageshake

import kotlinx.coroutines.flow.Flow

interface RageshakeDataStore {
    fun isEnabled(): Flow<Boolean>

    suspend fun setIsEnabled(isEnabled: Boolean)

    fun sensitivity(): Flow<Float>

    suspend fun setSensitivity(sensitivity: Float)

    suspend fun reset()
}
