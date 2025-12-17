/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.appnavstate.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultAppForegroundStateService : AppForegroundStateService {
    override val isInForeground = MutableStateFlow(false)
    override val isInCall = MutableStateFlow(false)
    override val isSyncingNotificationEvent = MutableStateFlow(false)
    override val hasRingingCall = MutableStateFlow(false)

    private val appLifecycle: Lifecycle by lazy { ProcessLifecycleOwner.get().lifecycle }

    override fun startObservingForeground() {
        appLifecycle.addObserver(lifecycleObserver)
    }

    override fun updateIsInCallState(isInCall: Boolean) {
        this.isInCall.value = isInCall
    }

    override fun updateHasRingingCall(hasRingingCall: Boolean) {
        this.hasRingingCall.value = hasRingingCall
    }

    override fun updateIsSyncingNotificationEvent(isSyncingNotificationEvent: Boolean) {
        this.isSyncingNotificationEvent.value = isSyncingNotificationEvent
    }

    private val lifecycleObserver = LifecycleEventObserver { _, _ -> isInForeground.value = getCurrentState() }

    private fun getCurrentState(): Boolean = appLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
}
