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
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.core.uri.isValidUrl
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Collections
import javax.inject.Inject

/**
 * Resolve homeserver base on search terms.
 */
class HomeserverResolver @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val wellknownRequest: WellknownRequest,
) {

    suspend fun resolve(userInput: String): Flow<List<HomeserverData>> = flow {
        val flowContext = currentCoroutineContext()
        val trimmedUserInput = userInput.trim()
        if (trimmedUserInput.length < 4) return@flow
        val candidateBase = trimmedUserInput.ensureProtocol().removeSuffix("/")
        val list = getUrlCandidates(candidateBase)
        val currentList = Collections.synchronizedList(mutableListOf<HomeserverData>())
        // Run all the requests in parallel
        withContext(dispatchers.io) {
            list.parallelMap { url ->
                val wellKnown = tryOrNull {
                    withTimeout(5000) {
                        wellknownRequest.execute(url)
                    }
                }
                val isValid = wellKnown?.isValid().orFalse()
                if (isValid) {
                    val supportSlidingSync = wellKnown?.supportSlidingSync().orFalse()
                    // Emit the list as soon as possible
                    currentList.add(
                        HomeserverData(
                            homeserverUrl = url,
                            isWellknownValid = true,
                            supportSlidingSync = supportSlidingSync
                        )
                    )
                    withContext(flowContext) {
                        emit(currentList.toList())
                    }
                }
            }
        }
        // If list is empty, and the user has entered an URL, do not block the user.
        if (currentList.isEmpty() && trimmedUserInput.isValidUrl()) {
            emit(
                listOf(
                    HomeserverData(
                        homeserverUrl = trimmedUserInput,
                        isWellknownValid = false,
                        supportSlidingSync = false,
                    )
                )
            )
        }
    }

    private fun getUrlCandidates(data: String): List<String> {
        return buildList {
            if (data.contains(".")) {
                // TLD detected?
            } else {
                add("$data.org")
                add("$data.com")
                add("$data.io")
            }
            // Always try what the user has entered
            add(data)
        }
    }
}
