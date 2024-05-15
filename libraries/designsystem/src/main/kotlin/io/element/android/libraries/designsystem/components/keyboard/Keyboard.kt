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

package io.element.android.libraries.designsystem.components.keyboard

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Inspired from https://stackoverflow.com/questions/68847559/how-can-i-detect-keyboard-opening-and-closing-in-jetpack-compose
 */
enum class Keyboard {
    Opened,
    Closed
}

// Note: it does not work as expected...
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun keyboardAsState(): State<Keyboard> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isResumed = lifecycle.currentState == Lifecycle.State.RESUMED
    return rememberUpdatedState(if (WindowInsets.isImeVisible && isResumed) Keyboard.Opened else Keyboard.Closed)
}
