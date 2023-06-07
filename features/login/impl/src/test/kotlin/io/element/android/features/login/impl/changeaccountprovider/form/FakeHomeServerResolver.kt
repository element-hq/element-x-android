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

import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.FAKE_DELAY_IN_MS
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHomeServerResolver : HomeserverResolver {
    private var pendingResult: List<List<HomeserverData>> = emptyList()
    fun givenResult(result: List<List<HomeserverData>>) {
        pendingResult = result
    }

    private val mutableFlow: MutableStateFlow<Async<List<HomeserverData>>> = MutableStateFlow(Async.Uninitialized)

    override fun flow(): StateFlow<Async<List<HomeserverData>>> = mutableFlow

    override suspend fun accept(userInput: String) {
        mutableFlow.tryEmit(Async.Uninitialized)
        delay(FAKE_DELAY_IN_MS)
        mutableFlow.tryEmit(Async.Loading())
        // Sending the pending result
        if (pendingResult.isEmpty()) {
            mutableFlow.tryEmit(Async.Uninitialized)
        } else {
            pendingResult.forEach {
                delay(FAKE_DELAY_IN_MS)
                mutableFlow.tryEmit(Async.Success(it))
            }
        }
    }
}
