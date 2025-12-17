/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.util

import com.freeletics.flowredux.dsl.InStateBuilderBlock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import com.freeletics.flowredux.dsl.State as MachineState

internal fun <T : Any> T.andLogStateChange() = also {
    Timber.w("Verification: state machine state moved to [${this::class.simpleName}]")
}

@OptIn(ExperimentalCoroutinesApi::class)
inline fun <State : Any, reified Event : Any> InStateBuilderBlock<State, State, Event>.logReceivedEvents() {
    on { event: Event, state: MachineState<State> ->
        Timber.w("Verification in state [${state.snapshot::class.simpleName}] receiving event [${event::class.simpleName}]")
        state.noChange()
    }
}
