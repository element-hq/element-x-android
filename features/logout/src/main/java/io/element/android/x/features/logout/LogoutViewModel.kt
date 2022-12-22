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

package io.element.android.x.features.logout

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.SessionScope
import io.element.android.x.matrix.MatrixClient
import kotlinx.coroutines.launch

@ContributesViewModel(SessionScope::class)
class LogoutViewModel @AssistedInject constructor(
    private val client: MatrixClient,
    @Assisted initialState: LogoutViewState
) : MavericksViewModel<LogoutViewState>(initialState) {

    companion object : MavericksViewModelFactory<LogoutViewModel, LogoutViewState> by daggerMavericksViewModelFactory()

    fun logout() {
        viewModelScope.launch {
            suspend {
                client.logout()
            }.execute {
                copy(logoutAction = it)
            }
        }
    }
}
