package io.element.android.x.architecture

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

inline fun <reified State> LifecycleOwner.presenterConnector(presenter: Presenter<State>): LifecyclePresenterConnector<State> =
    LifecyclePresenterConnector(lifecycleOwner = this, presenter = presenter)

class LifecyclePresenterConnector<State>(lifecycleOwner: LifecycleOwner, presenter: Presenter<State>) {

    private val moleculeScope = CoroutineScope(lifecycleOwner.lifecycleScope.coroutineContext + AndroidUiDispatcher.Main)

    val stateFlow: StateFlow<State> = moleculeScope.launchMolecule(RecompositionClock.Immediate) {
        presenter.present()
    }
}
