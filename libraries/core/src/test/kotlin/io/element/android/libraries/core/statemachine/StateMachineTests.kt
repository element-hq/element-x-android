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
        data class GoToSecond(val string: String) : Events

        object GoToThird : Events

        object GoToFourth : Events

        object Cancel : Events
    }

    sealed interface States {
        object First : States

        data class Second(val string: String) : States

        object Third : States

        object Fourth : States
        object Canceled : States
    }

    private var enteredSecondState = false
    private var exitedFirstState = false
    private var transitionHandlerParams: Triple<States, Events, States>? = null
    private fun aStateMachine() = createStateMachine<Events, States> {
        addInitialState(States.First) {
            onExit { exitedFirstState = true }
            on<Events.GoToSecond> { first, _ ->
                States.Second(first.string)
            }
        }
        addState<States.Second> {
            onEnter { enteredSecondState = true }
            on<Events.GoToThird>(States.Third)
        }

        addState<States.Fourth>()

        on<Events.GoToFourth, States.Fourth> { _, _ -> States.Fourth }
        on<Events.Cancel>(States.Canceled)
    }

    @Test
    fun `process - moves to next state given an event if the route exists`() = aStateMachine().run {
        process(Events.GoToSecond("Hello"))
        assertThat(currentState).isEqualTo(States.Second("Hello"))
        process(Events.GoToThird)
        assertThat(currentState).isEqualTo(States.Third)
        process(Events.GoToFourth)
        assertThat(currentState).isEqualTo(States.Fourth)
    }

    @Test
    fun `process - throws exception if there is no route for an event in a state`() = aStateMachine().run {
        runCatching {
            process(Events.GoToThird)
        }.onSuccess {
            fail("It should have thrown an error")
        }.onFailure {
            assertThat(it.message).startsWith("No route found for state")
        }
        Unit
    }

    @Test
    fun `process - calls onEnter and onExit callbacks when moving through states`() = aStateMachine().run {
        process(Events.GoToSecond("Hello"))
        assertThat(currentState).isEqualTo(States.Second("Hello"))

        assertThat(exitedFirstState).isTrue()
        assertThat(enteredSecondState).isTrue()
    }

    @Test
    fun `process - if an Event route is registered inside a state and outside it, the internal registration takes precedence`() {
        val customStateMachine = createStateMachine {
            addInitialState(States.First) {
                on<Events.Cancel>(States.Canceled)
            }
            on<Events.Cancel>(States.Fourth)
        }
        customStateMachine.process(Events.Cancel)
        assertThat(customStateMachine.currentState).isEqualTo(States.Canceled)
    }

    @Test
    fun `transitionHandler - is called when moving from a state to another`() = aStateMachine().run {
        transitionHandler = { from, event, to ->
            transitionHandlerParams = Triple(from, event, to)
        }

        process(Events.GoToSecond("Hello"))

        assertThat(transitionHandlerParams).isEqualTo(
            Triple(
                States.First,
                Events.GoToSecond("Hello"),
                States.Second("Hello"),
            )
        )
    }

    @Test
    fun `restart - sets the state machine to its initial state`() {
        val customStateMachine = createStateMachine {
            addInitialState(States.First)
            on<Events.GoToFourth>(States.Fourth)
        }
        customStateMachine.process(Events.GoToFourth)
        assertThat(customStateMachine.currentState).isEqualTo(States.Fourth)

        customStateMachine.restart()
        assertThat(customStateMachine.currentState).isEqualTo(customStateMachine.initialState)
    }

    @Test
    fun `init - the state machine must have registered a initial state`() {
        runCatching {
            createStateMachine<Events, States> {
                addState<States.Second>()
                on<Events.Cancel>(States.Canceled)
            }
        }.onSuccess {
            fail("It should have thrown an error")
        }.onFailure { error ->
            assertThat(error.message).isEqualTo("The state machine has no initial state")
        }
        Unit
    }

    @Test
    fun `init - the state machine having duplicate registrations for a state throws an error`() {
        runCatching {
            createStateMachine<Events, States> {
                addInitialState(States.First)
                addState<States.First>()
            }
        }.onSuccess {
            fail("It should have thrown an error")
        }.onFailure { error ->
            assertThat(error.message).startsWith("Duplicate registration for state ")
        }
        Unit
    }

    @Test
    fun `init - the state machine having duplicate registrations for an event inside a state throws an error`() {
        runCatching {
            createStateMachine<Events, States> {
                addInitialState(States.First) {
                    on<Events.GoToThird>(States.Third)
                    on<Events.GoToThird> { _, _ -> States.Third }
                }
            }
        }.onSuccess {
            fail("It should have thrown an error")
        }.onFailure { error ->
            assertThat(error.message).startsWith("Duplicate registration in state")
        }
        Unit
    }

    @Test
    fun `init - the state machine having duplicate registrations for an event at the root level throws an error`() {
        runCatching {
            createStateMachine<Events, States> {
                addInitialState(States.First)
                on<Events.GoToThird>(States.Third)
                on<Events.GoToThird>(States.Third)
            }
        }.onSuccess {
            fail("It should have thrown an error")
        }.onFailure { error ->
            assertThat(error.message).startsWith("Duplicate registration in state")
        }
        Unit
    }
}
