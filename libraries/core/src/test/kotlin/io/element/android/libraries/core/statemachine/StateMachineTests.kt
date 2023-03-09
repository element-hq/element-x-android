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

package io.element.android.libraries.core.statemachine

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test

class StateMachineTests {

    sealed interface Events {
        data class Start(val string: String): Events

        object Cancel: Events
    }

    sealed interface States {
        object Initial: States
        data class Final(val string: String): States
    }

    @Test
    fun `Test simple state machine`() {
        val stateMachine = createStateMachine<Events, States> {
            addInitialState(States.Initial) {
                on<Events.Start> { first, _ ->
                    States.Final(first.string)
                }
            }
            on<Events.Cancel>(States.Final("Canceled"))
        }

        stateMachine.process(Events.Start("Hello"))
        assertThat(stateMachine.currentState).isEqualTo(States.Final("Hello"))

        stateMachine.process(Events.Cancel)
        assertThat(stateMachine.currentState).isEqualTo(States.Final("Canceled"))

        runCatching {
            stateMachine.process(Events.Start("Invalid"))
        }.onSuccess {
            fail("It should have thrown an error")
        }.onFailure {
            assertThat(it.message).startsWith("No route found for state")
        }
    }

}
