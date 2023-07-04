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

package io.element.android.features.location.impl.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.location.api.Location
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ViewLocationPresenter @AssistedInject constructor(
    private val actions: LocationActions,
    @Assisted private val location: Location,
    @Assisted private val description: String?
) : Presenter<ViewLocationState> {

    @AssistedFactory
    interface Factory {
        fun create(location: Location, description: String?): ViewLocationPresenter
    }

    @Composable
    override fun present(): ViewLocationState {
        val coroutineScope = rememberCoroutineScope()
        actions.Configure()

        return ViewLocationState(
            location = location,
            description = description
        ) {
            when (it) {
                ViewLocationEvents.Share -> coroutineScope.share(location, description)
            }
        }
    }

    private fun CoroutineScope.share(location: Location, label: String?) = launch {
        actions.share(location, label)
    }
}
