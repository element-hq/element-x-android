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
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.core.uri.isValidUrl
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Resolve homeserver base on search terms
 */
class HomeserverResolver @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val wellknownRequest: WellknownRequest,
) {

    suspend fun resolve(userInput: String): List<HomeserverData> {
        return withContext(dispatchers.io) {
            val cleanedUpUserInput = userInput.trim()
            if (cleanedUpUserInput.length < 4) {
                // Wait for more chars
                emptyList()
            } else {
                val list = getUrlCandidate(cleanedUpUserInput)
                val resolvedList = resolveList(userInput, list)

                // If list is empty, and the user as entered an URL, do not block the user.
                if (resolvedList.isEmpty() && userInput.isValidUrl()) {
                    listOf(
                        HomeserverData(
                            userInput = userInput,
                            homeserverUrl = userInput,
                            isWellknownValid = false
                        )
                    )
                } else {
                    resolvedList
                }
            }
        }
    }

    private suspend fun resolveList(userInput: String, list: List<String>): List<HomeserverData> {
        return coroutineScope {
            buildList {
                list.map {
                    async {
                        val isValid = wellknownRequest.execute(it)
                        if (isValid) {
                            add(HomeserverData(userInput, it, true))
                        }
                    }
                }.joinAll()
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
