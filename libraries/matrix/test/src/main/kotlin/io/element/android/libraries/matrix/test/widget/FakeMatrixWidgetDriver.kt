/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
