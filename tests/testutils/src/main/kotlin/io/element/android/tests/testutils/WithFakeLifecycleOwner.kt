/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.tests.testutils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

@Stable
@Composable
fun <T> withFakeLifecycleOwner(lifecycleOwner: FakeLifecycleOwner = FakeLifecycleOwner(), block: @Composable () -> T): T {
    var state: T? by remember { mutableStateOf(null) }
    CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        state = block()
    }
    return state!!
}

@SuppressLint("VisibleForTests")
class FakeLifecycleOwner : LifecycleOwner {
    override val lifecycle: Lifecycle = LifecycleRegistry.createUnsafe(this)

    fun givenState(state: Lifecycle.State) {
        (lifecycle as LifecycleRegistry).currentState = state
    }
}
