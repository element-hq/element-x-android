/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.test.rageshake

import io.element.android.features.rageshake.api.rageshake.RageshakeDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

const val A_SENSITIVITY = 1f

class FakeRageshakeDataStore(
    isEnabled: Boolean = false,
    sensitivity: Float = A_SENSITIVITY,
) : RageshakeDataStore {
    private val isEnabledFlow = MutableStateFlow(isEnabled)
    override fun isEnabled(): Flow<Boolean> = isEnabledFlow

    override suspend fun setIsEnabled(isEnabled: Boolean) {
        isEnabledFlow.value = isEnabled
    }

    private val sensitivityFlow = MutableStateFlow(sensitivity)
    override fun sensitivity(): Flow<Float> = sensitivityFlow

    override suspend fun setSensitivity(sensitivity: Float) {
        sensitivityFlow.value = sensitivity
    }

    override suspend fun reset() = Unit
}
