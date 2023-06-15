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

package io.element.android.features.login.impl.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.error.ChangeServerError
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

class ChangeServerPresenter @Inject constructor(
    private val authenticationService: MatrixAuthenticationService,
    private val accountProviderDataSource: AccountProviderDataSource,
) : Presenter<ChangeServerState> {

    @Composable
    override fun present(): ChangeServerState {
        val localCoroutineScope = rememberCoroutineScope()

        val changeServerAction: MutableState<Async<Unit>> = remember {
            mutableStateOf(Async.Uninitialized)
        }

        fun handleEvents(event: ChangeServerEvents) {
            when (event) {
                is ChangeServerEvents.ChangeServer -> localCoroutineScope.changeServer(event.accountProvider, changeServerAction)
                ChangeServerEvents.ClearError -> changeServerAction.value = Async.Uninitialized
            }
        }

        return ChangeServerState(
            changeServerAction = changeServerAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.changeServer(
        data: AccountProvider,
        changeServerAction: MutableState<Async<Unit>>,
    ) = launch {
        suspend {
            val domain = tryOrNull { URL(data.title) }?.host ?: data.title
            authenticationService.setHomeserver(domain).map {
                authenticationService.getHomeserverDetails().value!!
                // Valid, remember user choice
                accountProviderDataSource.userSelection(data)
            }.getOrThrow()
        }.runCatchingUpdatingState(changeServerAction, exceptionTransform = ChangeServerError::from)
    }
}
