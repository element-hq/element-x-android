/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.test

import io.element.android.features.ptt.api.PttRoomService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePttRoomService(
    initialEnabled: Boolean = false,
) : PttRoomService {
    val isEnabled = MutableStateFlow(initialEnabled)

    override fun isPttEnabledFlow(): Flow<Boolean> = isEnabled

    override suspend fun setPttEnabled(enabled: Boolean) {
        isEnabled.value = enabled
    }
}
