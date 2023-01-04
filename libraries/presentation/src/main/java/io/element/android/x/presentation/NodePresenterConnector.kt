package io.element.android.x.presentation

import androidx.lifecycle.lifecycleScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.bumble.appyx.core.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

inline fun <reified State, reified Event> Node.presenterConnector(presenter: Presenter<State, Event>): NodePresenterConnector<State, Event> {
    return NodePresenterConnector(node = this, presenter = presenter)
}

class NodePresenterConnector<State, Event>(private val node: Node, presenter: Presenter<State, Event>) {

    private val moleculeScope = CoroutineScope(node.lifecycleScope.coroutineContext + AndroidUiDispatcher.Main)
    private val eventFlow: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 64)

    val stateFlow: StateFlow<State> = moleculeScope.launchMolecule(RecompositionClock.ContextClock) {
        presenter.present(events = eventFlow)
    }

    fun emitEvent(event: Event) {
        eventFlow.tryEmit(event)
    }
}
