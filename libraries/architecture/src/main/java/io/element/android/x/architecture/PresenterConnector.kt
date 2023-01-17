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
