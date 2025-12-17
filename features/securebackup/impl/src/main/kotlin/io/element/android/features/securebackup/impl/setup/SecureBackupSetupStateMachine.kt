/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("WildcardImport")
@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.securebackup.impl.setup

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.freeletics.flowredux.dsl.State as MachineState

@Inject
class SecureBackupSetupStateMachine : FlowReduxStateMachine<SecureBackupSetupStateMachine.State, SecureBackupSetupStateMachine.Event>(
    initialState = State.Initial
) {
    init {
        spec {
            inState<State.Initial> {
                on { _: Event.UserCreatesKey, state: MachineState<State.Initial> ->
                    state.override { State.CreatingKey }
                }
            }
            inState<State.CreatingKey> {
                on { event: Event.SdkError, state: MachineState<State.CreatingKey> ->
                    state.override { State.Error(event.exception) }
                }
                on { event: Event.SdkHasCreatedKey, state: MachineState<State.CreatingKey> ->
                    state.override { State.KeyCreated(event.key) }
                }
            }
            inState<State.KeyCreated> {
                on { _: Event.UserSavedKey, state: MachineState<State.KeyCreated> ->
                    state.override { State.KeyCreatedAndSaved(state.snapshot.key) }
                }
            }
            inState<State.Error> {
                on { _: Event.ClearError, state: MachineState<State.Error> ->
                    state.override { State.Initial }
                }
            }
            inState<State.KeyCreatedAndSaved> {
            }
        }
    }

    sealed interface State {
        data object Initial : State
        data object CreatingKey : State
        data class KeyCreated(val key: String) : State
        data class KeyCreatedAndSaved(val key: String) : State
        data class Error(val exception: Exception) : State
    }

    sealed interface Event {
        data object UserCreatesKey : Event
        data class SdkHasCreatedKey(val key: String) : Event
        data class SdkError(val exception: Exception) : Event
        data object UserSavedKey : Event
        data object ClearError : Event
    }
}
