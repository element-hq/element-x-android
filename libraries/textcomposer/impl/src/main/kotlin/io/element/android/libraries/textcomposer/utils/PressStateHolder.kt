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

package io.element.android.libraries.textcomposer.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber

@Composable
internal fun rememberPressState(
    longPressTimeoutMillis: Long = LocalViewConfiguration.current.longPressTimeoutMillis,
): PressStateHolder {
    return remember(longPressTimeoutMillis) {
        PressStateHolder(longPressTimeoutMillis = longPressTimeoutMillis)
    }
}

/**
 * State machine that keeps track of the pressed state.
 *
 * When a press is started, the state will transition through:
 * [PressState.Idle] -> [PressState.Tapping] -> ...
 *
 * If a press is held for a longer time, the state will continue through:
 * ... -> [PressState.LongPressing] -> ...
 *
 * When the press is released the states will then transition back to idle.
 * ... -> [PressState.Idle]
 *
 * Whether a press should be considered a tap or a long press can be determined by
 * looking at the last press when in the idle state.
 *
 * @see [PressStateEffects]
 * @see [rememberPressState]
 */
internal class PressStateHolder(
    private val longPressTimeoutMillis: Long,
) : State<PressState> {
    private var state: PressState by mutableStateOf(PressState.Idle(lastPress = null))

    override val value: PressState
        get() = state

    private var longPressTimer: Job? = null

    suspend fun press() = coroutineScope {
        when (state) {
            is PressState.Idle -> {
                state = PressState.Tapping
            }
            is PressState.Pressing ->
                Timber.e("Pointer pressed but it has not been released")
        }

        longPressTimer = launch {
            delay(longPressTimeoutMillis)
            yield()

            if (isActive && state == PressState.Tapping) {
                state = PressState.LongPressing
            }
        }
    }

    fun release() {
        longPressTimer?.cancel()
        longPressTimer = null
        when (val lastState = state) {
            is PressState.Pressing ->
                state = PressState.Idle(lastPress = lastState)
            is PressState.Idle ->
                Timber.e("Pointer pressed but it has not been released")
        }
    }
}

