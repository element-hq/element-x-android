/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.test

import io.element.android.features.lockscreen.api.LockScreenLockState
import io.element.android.features.lockscreen.api.LockScreenService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class FakeLockScreenService : LockScreenService {
    private var isPinSetup = MutableStateFlow(false)
    private val _lockState: MutableStateFlow<LockScreenLockState> = MutableStateFlow(LockScreenLockState.Locked)
    override val lockState: StateFlow<LockScreenLockState> = _lockState

    override fun isSetupRequired(): Flow<Boolean> {
        return isPinSetup.map { !it }
    }

    fun setIsPinSetup(isPinSetup: Boolean) {
        this.isPinSetup.value = isPinSetup
    }

    override fun isPinSetup(): Flow<Boolean> {
        return isPinSetup
    }

    fun setLockState(lockState: LockScreenLockState) {
        _lockState.value = lockState
    }
}
