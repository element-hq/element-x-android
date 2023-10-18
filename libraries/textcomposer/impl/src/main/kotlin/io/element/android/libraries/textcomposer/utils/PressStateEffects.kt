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
import androidx.compose.runtime.LaunchedEffect

/**
 * React to [PressState] changes.
 */
@Composable
internal fun PressStateEffects(
    pressState: PressState,
    onPressStart: () -> Unit = {},
    onLongPressStart: () -> Unit = {},
    onTap: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
) {
    LaunchedEffect(pressState) {
        when (pressState) {
            is PressState.Idle ->
                when (pressState.lastPress) {
                    PressState.Tapping -> onTap()
                    PressState.LongPressing -> onLongPressEnd()
                    null -> {} // Do nothing
                }
            is PressState.LongPressing -> onLongPressStart()
            PressState.Tapping -> onPressStart()
        }
    }
}


