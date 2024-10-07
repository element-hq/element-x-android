/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    fun resolve(userInput: String): Flow<List<HomeserverData>> = flow {
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
                    // Emit the list as soon as possible
                    currentList.add(
                        HomeserverData(
                            homeserverUrl = url,
                            isWellknownValid = true,
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
