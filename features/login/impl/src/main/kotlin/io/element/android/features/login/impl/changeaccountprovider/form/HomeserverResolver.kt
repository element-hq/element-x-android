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

import io.element.android.features.login.impl.changeaccountprovider.form.network.WellknownRequest
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.core.uri.isValidUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Resolve homeserver base on search terms
 */
class HomeserverResolver @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val wellknownRequest: WellknownRequest,
) {
    private val mutableFlow: MutableStateFlow<Async<List<HomeserverData>>> = MutableStateFlow(Async.Uninitialized)

    fun flow(): StateFlow<Async<List<HomeserverData>>> = mutableFlow

    private var currentJob: Job? = null

    suspend fun accept(userInput: String) {
        currentJob?.cancel()
        val cleanedUpUserInput = userInput.trim()
        mutableFlow.tryEmit(Async.Uninitialized)
        if (cleanedUpUserInput.length > 3) {
            delay(300)
            mutableFlow.tryEmit(Async.Loading())
            withContext(dispatchers.io) {
                val list = getUrlCandidate(cleanedUpUserInput)
                currentJob = resolveList(userInput, list)
            }
        }
    }

    private fun CoroutineScope.resolveList(userInput: String, list: List<String>): Job {
        val currentList = mutableListOf<HomeserverData>()
        return launch {
            list.map {
                async {
                    val isValid = tryOrNull { wellknownRequest.execute(it) }.orFalse()
                    if (isValid) {
                        // Emit the list as soon as possible
                        currentList.add(HomeserverData(userInput, it, true))
                        mutableFlow.tryEmit(Async.Success(currentList))
                    }
                }
            }.joinAll()
                .also {
                    // If list is empty, and the user as entered an URL, do not block the user.
                    if (currentList.isEmpty()) {
                        if (userInput.isValidUrl()) {
                            mutableFlow.tryEmit(
                                Async.Success(
                                    listOf(
                                        HomeserverData(
                                            userInput = userInput,
                                            homeserverUrl = userInput,
                                            isWellknownValid = false
                                        )
                                    )
                                )
                            )
                        } else {
                            mutableFlow.tryEmit(Async.Uninitialized)
                        }
                    }
                }
        }
    }

    private fun getUrlCandidate(data: String): List<String> {
        return buildList {
            val s = data.ensureProtocol()
                .removeSuffix("/")

            // Always try what the user has entered
            add(s)

            if (s.contains(".")) {
                // TLD detected?
            } else {
                add("$s.org")
                add("$s.com")
                add("$s.io")
            }
        }
    }
}
