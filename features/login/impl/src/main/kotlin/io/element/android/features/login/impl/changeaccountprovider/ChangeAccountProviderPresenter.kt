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

package io.element.android.features.login.impl.changeaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.login.impl.changeserver.ChangeServerError
import io.element.android.features.login.impl.util.LoginConstants
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.execute
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

class ChangeAccountProviderPresenter @Inject constructor(
    private val authenticationService: MatrixAuthenticationService
) : Presenter<ChangeAccountProviderState> {

    @Composable
    override fun present(): ChangeAccountProviderState {
        val localCoroutineScope = rememberCoroutineScope()

        val homeserver = rememberSaveable {
            mutableStateOf(authenticationService.getHomeserverDetails().value?.url ?: LoginConstants.DEFAULT_HOMESERVER_URL)
        }
        val changeServerAction: MutableState<Async<Unit>> = remember {
            mutableStateOf(Async.Uninitialized)
        }

        fun handleEvents(event: ChangeAccountProviderEvents) {
            when (event) {
                is ChangeAccountProviderEvents.SetServer -> {
                    homeserver.value = event.server
                    handleEvents(ChangeAccountProviderEvents.ClearError)
                }
                ChangeAccountProviderEvents.Submit -> {
                    localCoroutineScope.submit(homeserver, changeServerAction)
                }
                ChangeAccountProviderEvents.ClearError -> changeServerAction.value = Async.Uninitialized
            }
        }

        return ChangeAccountProviderState(
            homeserver = homeserver.value,
            changeServerAction = changeServerAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.submit(homeserverUrl: MutableState<String>, changeServerAction: MutableState<Async<Unit>>) = launch {
        suspend {
            val domain = tryOrNull { URL(homeserverUrl.value) }?.host ?: homeserverUrl.value
            authenticationService.setHomeserver(domain).getOrThrow()
            homeserverUrl.value = domain
        }.execute(changeServerAction, errorMapping = ChangeServerError::from)
    }
}
