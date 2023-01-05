package io.element.android.x.architecture

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

inline fun <reified State, reified Event> LifecycleOwner.presenterConnector(presenter: Presenter<State, Event>): Lazy<LifecyclePresenterConnector<State, Event>> = lazy {
    LifecyclePresenterConnector(lifecycleOwner = this, presenter = presenter)
}

class LifecyclePresenterConnector<State, Event>(lifecycleOwner: LifecycleOwner, presenter: Presenter<State, Event>) {

    private val moleculeScope = CoroutineScope(lifecycleOwner.lifecycleScope.coroutineContext + AndroidUiDispatcher.Main)
    private val eventFlow: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 64)

    val stateFlow: StateFlow<State> = moleculeScope.launchMolecule(RecompositionClock.ContextClock) {
        presenter.present(events = eventFlow)
    }

    fun emitEvent(event: Event) {
        eventFlow.tryEmit(event)
    }
}
