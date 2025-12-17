/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.test

import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.ftue.api.state.FtueState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFtueService : FtueService {
    override val state: MutableStateFlow<FtueState> = MutableStateFlow(FtueState.Unknown)

    suspend fun emitState(newState: FtueState) {
        state.emit(newState)
    }
}
