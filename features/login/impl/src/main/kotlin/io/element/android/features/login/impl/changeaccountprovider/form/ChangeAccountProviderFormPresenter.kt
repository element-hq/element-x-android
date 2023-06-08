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

package io.element.android.features.login.impl.changeaccountprovider.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.login.impl.changeaccountprovider.common.ChangeServerPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class ChangeAccountProviderFormPresenter @Inject constructor(
    private val homeserverResolver: HomeserverResolver,
    private val changeServerPresenter: ChangeServerPresenter,
) : Presenter<ChangeAccountProviderFormState> {

    @Composable
    override fun present(): ChangeAccountProviderFormState {
        var userInput by rememberSaveable {
            mutableStateOf("")
        }
        val changeServerState = changeServerPresenter.present()

        var data: Async<List<HomeserverData>> by remember {
            mutableStateOf(Async.Uninitialized)
        }

        LaunchedEffect(userInput) {
            homeserverResolver.resolve(userInput).collect {
                data = it
            }
        }

        fun handleEvents(event: ChangeAccountProviderFormEvents) {
            when (event) {
                is ChangeAccountProviderFormEvents.UserInput -> {
                    userInput = event.input
                }
            }
        }

        return ChangeAccountProviderFormState(
            userInput = userInput,
            userInputResult = data,
            changeServerState = changeServerState,
            eventSink = ::handleEvents
        )
    }
}
