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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeHomeServerResolver : HomeserverResolver {
    private var pendingResult: List<List<HomeserverData>> = emptyList()
    fun givenResult(result: List<List<HomeserverData>>) {
        pendingResult = result
    }

    override suspend fun resolve(userInput: String): Flow<Async<List<HomeserverData>>> = flow {
        emit(Async.Uninitialized)
        delay(FAKE_DELAY_IN_MS)
        emit(Async.Loading())
        // Sending the pending result
        if (pendingResult.isEmpty()) {
            emit(Async.Uninitialized)
        } else {
            pendingResult.forEach {
                delay(FAKE_DELAY_IN_MS)
                emit(Async.Success(it))
            }
        }
    }
}
