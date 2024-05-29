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

package io.element.android.libraries.matrix.test.widget

import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.UUID

class FakeMatrixWidgetDriver(
    override val id: String = UUID.randomUUID().toString(),
) : MatrixWidgetDriver {
    private val _sentMessages = mutableListOf<String>()
    val sentMessages: List<String> = _sentMessages

    var runCalledCount = 0
        private set
    var closeCalledCount = 0
        private set

    override val incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override suspend fun run() {
        runCalledCount++
    }

    override suspend fun send(message: String) {
        _sentMessages.add(message)
    }

    override fun close() {
        closeCalledCount++
    }

    fun givenIncomingMessage(message: String) {
        incomingMessages.tryEmit(message)
    }
}
