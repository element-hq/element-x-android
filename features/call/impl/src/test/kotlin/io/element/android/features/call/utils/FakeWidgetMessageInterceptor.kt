/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.utils

import io.element.android.features.call.impl.utils.WidgetMessageInterceptor
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeWidgetMessageInterceptor : WidgetMessageInterceptor {
    val sentMessages = mutableListOf<String>()

    override val interceptedMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun sendMessage(message: String) {
        sentMessages += message
    }

    fun givenInterceptedMessage(message: String) {
        interceptedMessages.tryEmit(message)
    }
}
