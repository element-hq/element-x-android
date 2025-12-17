/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.login.impl.changeserver.ChangeServerState
import io.element.android.features.login.impl.resolver.HomeserverData
import io.element.android.features.login.impl.resolver.HomeserverResolver
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
class SearchAccountProviderPresenter(
    private val homeserverResolver: HomeserverResolver,
    private val changeServerPresenter: Presenter<ChangeServerState>,
) : Presenter<SearchAccountProviderState> {
    @Composable
    override fun present(): SearchAccountProviderState {
        var userInput by rememberSaveable {
            mutableStateOf("")
        }
        val changeServerState = changeServerPresenter.present()

        val data: MutableState<AsyncData<List<HomeserverData>>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        LaunchedEffect(userInput) {
            onUserInput(userInput, data)
        }

        fun handleEvent(event: SearchAccountProviderEvents) {
            when (event) {
                is SearchAccountProviderEvents.UserInput -> {
                    userInput = event.input
                }
            }
        }

        return SearchAccountProviderState(
            userInput = userInput,
            userInputResult = data.value,
            changeServerState = changeServerState,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.onUserInput(userInput: String, data: MutableState<AsyncData<List<HomeserverData>>>) = launch {
        data.value = AsyncData.Uninitialized
        // Debounce
        delay(500)
        data.value = AsyncData.Loading()
        homeserverResolver.resolve(userInput).collect {
            data.value = AsyncData.Success(it)
        }
        if (data.value !is AsyncData.Success) {
            data.value = AsyncData.Uninitialized
        }
    }
}
