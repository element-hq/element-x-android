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
