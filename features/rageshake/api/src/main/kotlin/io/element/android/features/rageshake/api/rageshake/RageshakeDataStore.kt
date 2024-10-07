/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.api.rageshake

import kotlinx.coroutines.flow.Flow

interface RageshakeDataStore {
    fun isEnabled(): Flow<Boolean>

    suspend fun setIsEnabled(isEnabled: Boolean)

    fun sensitivity(): Flow<Float>

    suspend fun setSensitivity(sensitivity: Float)

    suspend fun reset()
}
