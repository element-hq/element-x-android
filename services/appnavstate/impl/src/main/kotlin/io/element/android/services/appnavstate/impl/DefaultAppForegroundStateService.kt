/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
