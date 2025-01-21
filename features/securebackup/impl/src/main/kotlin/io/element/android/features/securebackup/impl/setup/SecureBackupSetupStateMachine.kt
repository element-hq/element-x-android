/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("WildcardImport")
@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.securebackup.impl.setup

import com.freeletics.flowredux.dsl.FlowReduxStateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import com.freeletics.flowredux.dsl.State as MachineState

class SecureBackupSetupStateMachine @Inject constructor() : FlowReduxStateMachine<SecureBackupSetupStateMachine.State, SecureBackupSetupStateMachine.Event>(
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
                on { _: Event.SdkError, state: MachineState<State.CreatingKey> ->
                    state.override { State.Initial }
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
            inState<State.KeyCreatedAndSaved> {
            }
        }
    }

    sealed interface State {
        data object Initial : State
        data object CreatingKey : State
        data class KeyCreated(val key: String) : State
        data class KeyCreatedAndSaved(val key: String) : State
    }

    sealed interface Event {
        data object UserCreatesKey : Event
        data class SdkHasCreatedKey(val key: String) : Event
        data class SdkError(val throwable: Throwable) : Event
        data object UserSavedKey : Event
    }
}
