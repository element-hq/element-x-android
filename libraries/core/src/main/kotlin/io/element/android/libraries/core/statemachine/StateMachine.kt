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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

fun <Event : Any, State : Any> createStateMachine(
    config: StateMachineBuilder<Event, State>.() -> Unit
): StateMachine<Event, State> {
    val builder = StateMachineBuilder<Event, State>()
    config(builder)
    return builder.build()
}

class StateMachine<Event : Any, State : Any>(
    val initialState: State,
    private val stateConfigs: Map<Class<*>, StateConfig<*>>,
    private val routes: List<StateMachineRoute<*, *, *>>,
) {

    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow = _stateFlow.asStateFlow()
    val currentState: State get() = stateFlow.value

    var transitionHandler: ((State, Event, State) -> Unit)? = null

    init {
        @Suppress("UNCHECKED_CAST")
        (stateConfigs[initialState::class.java] as? StateConfig<State>)?.onEnter?.invoke(initialState)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Event> process(event: E) {
        val route = routes.firstOrNull { route ->
            ((route.fromState == null || route.fromState.isInstance(currentState)) && route.eventType.isInstance(event))
        }  as? StateMachineRoute<E, State, State>
            ?: error("No route found for state $currentState on event $event")

        val lastStateConfig: StateConfig<State>? = stateConfigs[currentState::class.java] as? StateConfig<State>
        lastStateConfig?.onExit?.invoke(currentState)

        val nextState = route.toState(event, currentState)
        transitionHandler?.invoke(currentState, event, nextState)
        _stateFlow.value = nextState

        val currentStateConfig = stateConfigs[nextState::class.java] as? StateConfig<State>
        currentStateConfig?.onEnter?.invoke(nextState)
    }
}

class StateMachineBuilder<Event : Any, State : Any>(
    val routes: MutableList<StateMachineRoute<out Event, out State, out State>> = mutableListOf(),
) {

    lateinit var initialState: State
    var stateConfigs = mutableMapOf<Class<out State>, StateConfig<out State>>()

    inline fun <reified S : State> addState(block: StateRegistrationBuilder<Event, State, S>.() -> Unit = {}) {
        val config = StateConfig(S::class.java)
        val registrationBuilder = StateRegistrationBuilder<Event, State, S>(config)
        block(registrationBuilder)
        stateConfigs[S::class.java] = config
        routes.addAll(registrationBuilder.routes)
    }

    inline fun <reified S : State> addInitialState(state: S, config: StateRegistrationBuilder<Event, State, S>.() -> Unit = {}) {
        initialState = state
        addState(block = config)
    }

    inline fun <reified E : Event, reified S : State> on(noinline configuration: (E, State) -> State) {
        val builder = RouteBuilder<E, S, State>(E::class.java, null)
        builder.toState = configuration
        routes.add(builder.build())
    }

    inline fun <reified E : Event> on(newState: State) {
        val builder = RouteBuilder<E, State, State>(E::class.java, null)
        builder.toState = { _, _ -> newState }
        routes.add(builder.build())
    }

    fun build(): StateMachine<Event, State> {
        return StateMachine(initialState, stateConfigs.toMap(), routes)
    }
}

class StateRegistrationBuilder<Event : Any, BaseState : Any, State : BaseState>(
    val fromState: StateConfig<State>,
    val routes: MutableList<StateMachineRoute<out Event, out State, out BaseState>> = mutableListOf(),
) {

    fun onEnter(enter: (State) -> Unit) {
        fromState.onEnter = enter
    }

    fun onExit(exit: (State) -> Unit) {
        fromState.onExit = exit
    }

    inline fun <reified E : Event> on(noinline configuration: (E, State) -> BaseState) {
        val builder = RouteBuilder<E, State, BaseState>(E::class.java, fromState.state)
        builder.toState = configuration
        routes.add(builder.build())
    }

    inline fun <reified E : Event, To : State> on(newState: To) {
        val builder = RouteBuilder<E, State, To>(E::class.java, fromState.state)
        builder.toState = { _, _ -> newState }
        routes.add(builder.build())
    }
}

class RouteBuilder<Event : Any, FromState : Any, ToState : Any>(
    val eventType: Class<out Event>,
    val fromState: Class<out FromState>?,
) {
    lateinit var toState: (Event, FromState) -> ToState

    fun build() = StateMachineRoute(eventType, fromState, toState)
}

data class StateMachineRoute<Event : Any, FromState : Any, ToState : Any>(
    val eventType: Class<out Event>,
    val fromState: Class<out FromState>?,
    val toState: (Event, FromState) -> ToState,
)

data class StateConfig<State : Any>(
    val state: Class<State>,
    var onEnter: ((State) -> Unit)? = null,
    var onExit: ((State) -> Unit)? = null,
)
