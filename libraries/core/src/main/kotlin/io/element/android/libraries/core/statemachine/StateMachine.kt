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

import io.element.android.libraries.core.bool.orFalse
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
    private val logger: ((String) -> Unit)? = null,
) {

    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow = _stateFlow.asStateFlow()
    val currentState: State get() = stateFlow.value

    var transitionHandler: ((State, Event, State) -> Unit)? = null

    init {
        @Suppress("UNCHECKED_CAST")
        val initialStateConfig = stateConfigs[initialState::class.java] as StateConfig<State>
        initialStateConfig.onEnter?.invoke(initialState)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Event> process(event: E) {
        val route = findMatchingRoute(event) ?: error("No route found for state $currentState on event $event")

        val nextState = route.toState(event, currentState)

        logTransition(currentState, nextState, event)

        val lastStateConfig: StateConfig<State>? = stateConfigs[currentState::class.java] as? StateConfig<State>
        lastStateConfig?.onExit?.invoke(currentState)

        transitionHandler?.invoke(currentState, event, nextState)
        _stateFlow.value = nextState

        val currentStateConfig = stateConfigs[nextState::class.java] as? StateConfig<State>
        currentStateConfig?.onEnter?.invoke(nextState)
    }

    private fun <E : Event> findMatchingRoute(event: E): StateMachineRoute<E, State, State>? {
        val routesForEvent = routes.filter { it.eventType.isInstance(event) }

        return (routesForEvent.firstOrNull { it.fromState?.isInstance(currentState).orFalse() }
                ?: routesForEvent.firstOrNull { it.fromState == null }) as? StateMachineRoute<E, State, State>
    }

    fun restart() {
        _stateFlow.value = initialState
    }

    private fun logTransition(fromState: State, toState: State, event: Event) {
        val logger = this.logger ?: return
        val fromStateName = if (fromState::class.objectInstance != null) {
            fromState::class.simpleName
        } else {
            fromState.toString()
        }
        val nextStateName = if (toState::class.objectInstance != null) {
            toState::class.simpleName
        } else {
            toState.toString()
        }
        val eventName = if (event::class.objectInstance != null) {
            event::class.simpleName
        } else {
            event.toString()
        }
        logger.invoke("State: $fromStateName -> $nextStateName. Event: $eventName")
    }
}

class StateMachineBuilder<Event : Any, State : Any>(
    val routes: MutableList<StateMachineRoute<out Event, out State, out State>> = mutableListOf(),
) {

    lateinit var initialState: State
    var stateConfigs = mutableMapOf<Class<out State>, StateConfig<out State>>()

    var logger: ((String) -> Unit)? = null

    inline fun <reified S : State> addState(block: StateRegistrationBuilder<Event, State, S>.() -> Unit = {}) {
        val config = StateConfig(S::class.java)
        val registrationBuilder = StateRegistrationBuilder<Event, State, S>(config)
        block(registrationBuilder)

        verifyRoutesAreUnique(S::class.java, routes, registrationBuilder.routes)

        if (stateConfigs.contains(S::class.java)) {
            error("Duplicate registration for state ${S::class.java.name}")
        }
        stateConfigs[S::class.java] = config
        routes.addAll(registrationBuilder.routes)
    }

    inline fun <reified S : State> addInitialState(state: S, config: StateRegistrationBuilder<Event, State, S>.() -> Unit = {}) {
        initialState = state
        addState(block = config)
    }

    inline fun <reified E : Event, reified S : State> on(noinline configuration: (E, State) -> S) {
        val builder = RouteBuilder<E, State, S>(E::class.java, null)
        builder.toState = configuration
        val newRoute = builder.build()
        verifyRoutesAreUnique(S::class.java, routes, listOf(newRoute))
        routes.add(newRoute)
    }

    inline fun <reified E : Event> on(newState: State) {
        val builder = RouteBuilder<E, State, State>(E::class.java, null)
        builder.toState = { _, _ -> newState }
        val newRoute = builder.build()
        verifyRoutesAreUnique(null, routes, listOf(newRoute))
        routes.add(newRoute)
    }

    fun build(): StateMachine<Event, State> {
        if (::initialState.isInitialized) {
            return StateMachine(initialState, stateConfigs.toMap(), routes, logger)
        } else {
            error("The state machine has no initial state")
        }
    }

    companion object {
        fun verifyRoutesAreUnique(
            state: Class<*>?,
            oldRoutes: List<StateMachineRoute<*, *, *>>,
            newRoutes: List<StateMachineRoute<*, *, *>>,
        ) {
            val oldEvents = oldRoutes.filter { it.fromState == state }.map { it.eventType }
            val newEvents = newRoutes.filter { it.fromState == state  }.map { it.eventType }
            val intersection = oldEvents.intersect(newEvents)
            if (intersection.isNotEmpty()) {
                val duplicates = intersection.joinToString(", ") { it.name }
                error("Duplicate registration in state ${state?.name} for events: $duplicates")
            }
        }
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
        val newRoute = builder.build()
        StateMachineBuilder.verifyRoutesAreUnique(fromState.state, routes, listOf(newRoute))
        routes.add(newRoute)
    }

    inline fun <reified E : Event> on(newState: BaseState) {
        val builder = RouteBuilder<E, State, BaseState>(E::class.java, fromState.state)
        builder.toState = { _, _ -> newState }
        val newRoute = builder.build()
        StateMachineBuilder.verifyRoutesAreUnique(fromState.state, routes, listOf(newRoute))
        routes.add(newRoute)
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
