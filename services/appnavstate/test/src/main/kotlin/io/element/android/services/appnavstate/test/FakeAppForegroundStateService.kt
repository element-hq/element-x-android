/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.appnavstate.test

import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAppForegroundStateService(
    initialValue: Boolean = true,
) : AppForegroundStateService {
    private val state = MutableStateFlow(initialValue)
    override val isInForeground: StateFlow<Boolean> = state

    override fun start() {
        // No-op
    }

    fun givenIsInForeground(isInForeground: Boolean) {
        state.value = isInForeground
    }
}
