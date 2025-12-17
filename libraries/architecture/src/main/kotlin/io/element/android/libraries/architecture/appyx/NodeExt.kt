/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(InternalComposeApi::class)

package io.element.android.libraries.architecture.appyx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.currentComposer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.bumble.appyx.core.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

fun <State> Node.launchMolecule(body: @Composable () -> State): StateFlow<State> {
    val scope = CoroutineScope(lifecycleScope.coroutineContext + AndroidUiDispatcher.Main)
    return scope.launchMolecule(mode = RecompositionMode.ContextClock) {
        currentComposer.startProviders(
            values = arrayOf(LocalLifecycleOwner provides this),
        )
        val state = body()
        currentComposer.endProviders()
        state
    }
}
