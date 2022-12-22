/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.core.di

import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext

/**
 * To connect Mavericks ViewModel creation with Anvil's dependency injection, add the following to your MavericksViewModel.
 *
 * Example:
 *
 * @ContributesViewModel(YourScope::class)
 * class MyViewModel @AssistedInject constructor(
 *     @Assisted initialState: MyState,
 *     …,
 * ): MavericksViewModel<MyState>(...) {
 *     …
 *
 *     companion object : MavericksViewModelFactory<MyViewModel, MyState> by daggerMavericksViewModelFactory()
 * }
 */

inline fun <reified VM : MavericksViewModel<S>, S : MavericksState> daggerMavericksViewModelFactory() = DaggerMavericksViewModelFactory(VM::class.java)

/**
 * A [MavericksViewModelFactory] makes it easy to create instances of a ViewModel
 * using its AssistedInject Factory. This class should be implemented by the companion object
 * of every ViewModel which uses AssistedInject via [daggerMavericksViewModelFactory].
 *
 * @param VM The ViewModel type
 * @param S The ViewState type
 * @param viewModelClass The [Class] of the ViewModel being requested for creation
 *
 * This class accesses the map of ViewModel class to [AssistedViewModelFactory]s from the nearest [DaggerComponentOwner] and
 * uses it to retrieve the requested ViewModel's factory class. It then creates an instance of this ViewModel
 * using the retrieved factory and returns it.
 * @see daggerMavericksViewModelFactory
 */
class DaggerMavericksViewModelFactory<VM : MavericksViewModel<S>, S : MavericksState>(
    private val viewModelClass: Class<VM>
) : MavericksViewModelFactory<VM, S> {

    override fun create(viewModelContext: ViewModelContext, state: S): VM {
        val bindings: DaggerMavericksBindings = when (viewModelContext) {
            is FragmentViewModelContext -> viewModelContext.fragment.bindings()
            else -> viewModelContext.activity.bindings()
        }
        val viewModelFactoryMap = bindings.viewModelFactories()
        val viewModelFactory = viewModelFactoryMap[viewModelClass] ?: error("Cannot find ViewModelFactory for ${viewModelClass.name}.")

        @Suppress("UNCHECKED_CAST")
        val castedViewModelFactory = viewModelFactory as? AssistedViewModelFactory<VM, S>
        val viewModel = castedViewModelFactory?.create(state)
        return viewModel as VM
    }
}

interface DaggerMavericksBindings {
    fun viewModelFactories(): Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
}
