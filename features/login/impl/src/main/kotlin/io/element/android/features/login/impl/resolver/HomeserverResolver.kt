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

package io.element.android.features.login.impl.resolver

import io.element.android.features.login.impl.resolver.network.WellknownRequest
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.core.uri.isValidUrl
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject

/**
 * Resolve homeserver base on search terms.
 */
class HomeserverResolver @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val wellknownRequest: WellknownRequest,
) {

    suspend fun resolve(userInput: String): Flow<Async<List<HomeserverData>>> = flow {
        val flowContext = currentCoroutineContext()
        emit(Async.Uninitialized)
        // Debounce
        delay(300)
        val trimmedUserInput = userInput.trim()
        if (trimmedUserInput.length < 4) return@flow
        emit(Async.Loading())
        val candidateBase = trimmedUserInput.ensureProtocol().removeSuffix("/")
        val list = getUrlCandidates(candidateBase)
        val currentList = Collections.synchronizedList(mutableListOf<HomeserverData>())
        // Run all the requests in parallel
        withContext(dispatchers.io) {
            list.map {
                async {
                    val wellKnown = tryOrNull { wellknownRequest.execute(it) }
                    val isValid = wellKnown?.isValid().orFalse()
                    if (isValid) {
                        val supportSlidingSync = wellKnown?.supportSlidingSync().orFalse()
                        // Emit the list as soon as possible
                        currentList.add(
                            HomeserverData(
                                homeserverUrl = it,
                                isWellknownValid = true,
                                supportSlidingSync = supportSlidingSync
                            )
                        )
                        withContext(flowContext) {
                            emit(Async.Success(currentList))
                        }
                    }
                }
            }.awaitAll()
        }
        // If list is empty, and the user has entered an URL, do not block the user.
        if (currentList.isEmpty()) {
            if (trimmedUserInput.isValidUrl()) {
                emit(
                    Async.Success(
                        listOf(
                            HomeserverData(
                                homeserverUrl = trimmedUserInput,
                                isWellknownValid = false,
                                supportSlidingSync = false,
                            )
                        )
                    )
                )
            } else {
                emit(Async.Uninitialized)
            }
        }
    }

    private fun getUrlCandidates(data: String): List<String> {
        return buildList {
            if (data.contains(".")) {
                // TLD detected?
            } else {
                add("${data}.org")
                add("${data}.com")
                add("${data}.io")
            }
            // Always try what the user has entered
            add(data)
        }
    }
}
