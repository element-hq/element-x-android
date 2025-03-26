/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.element.android.libraries.architecture.Presenter

/**
 * Composable that provides a fake [LifecycleOwner] to the composition.
 *
 * **WARNING: DO NOT USE OUTSIDE TESTS.**
 */
@OptIn(InternalComposeApi::class)
@Stable
@Composable
fun <T> withFakeLifecycleOwner(
    lifecycleOwner: FakeLifecycleOwner = FakeLifecycleOwner(),
    block: @Composable () -> T
): T {
    currentComposer.startProvider(LocalLifecycleOwner provides lifecycleOwner)
    val state = block()
    currentComposer.endProvider()
    return state
}

/**
 * Test a [Presenter] with a fake [LifecycleOwner].
 *
 * **WARNING: DO NOT USE OUTSIDE TESTS.**
 */
suspend fun <T> Presenter<T>.testWithLifecycleOwner(
    lifecycleOwner: FakeLifecycleOwner = FakeLifecycleOwner(),
    block: suspend TurbineTestContext<T>.() -> Unit
) {
    moleculeFlow(RecompositionMode.Immediate) {
        val ret = withFakeLifecycleOwner(lifecycleOwner) {
            present()
        }
        ret
    }.test<T>(validate = block)
}

@SuppressLint("VisibleForTests")
class FakeLifecycleOwner(initialState: Lifecycle.State? = null) : LifecycleOwner {
    override val lifecycle: Lifecycle = LifecycleRegistry.createUnsafe(this)

    init {
        initialState?.let { givenState(it) }
    }

    fun givenState(state: Lifecycle.State) {
        (lifecycle as LifecycleRegistry).currentState = state
    }
}
