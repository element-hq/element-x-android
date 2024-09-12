/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner

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
