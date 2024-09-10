/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.appnavstate.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DefaultAppForegroundStateService : AppForegroundStateService {
    private val state = MutableStateFlow(false)
    override val isInForeground: StateFlow<Boolean> = state

    private val appLifecycle: Lifecycle by lazy { ProcessLifecycleOwner.get().lifecycle }

    override fun start() {
        appLifecycle.addObserver(lifecycleObserver)
    }

    private val lifecycleObserver = LifecycleEventObserver { _, _ -> state.value = getCurrentState() }

    private fun getCurrentState(): Boolean = appLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
}
