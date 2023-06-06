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

package io.element.android.features.login.impl.accountprovider

import androidx.compose.runtime.Composable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter

data class AccountProviderPresenterParams(
    val homeserver: String,
    val isMatrixOrg: Boolean,
    val isAccountCreation: Boolean,
)

class AccountProviderPresenter @AssistedInject constructor(
    @Assisted private val params: AccountProviderPresenterParams,
) : Presenter<AccountProviderState> {

    @AssistedFactory
    interface Factory {
        fun create(params: AccountProviderPresenterParams): AccountProviderPresenter
    }

    @Composable
    override fun present(): AccountProviderState {

        fun handleEvents(event: AccountProviderEvents) {
            when (event) {
                AccountProviderEvents.MyEvent -> Unit
            }
        }

        return AccountProviderState(
            homeserver = params.homeserver,
            isMatrix = params.isMatrixOrg,
            isAccountCreation = params.isAccountCreation,
            eventSink = ::handleEvents
        )
    }
}
