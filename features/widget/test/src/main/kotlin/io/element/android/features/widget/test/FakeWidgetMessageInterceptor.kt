/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.test

import io.element.android.features.widget.impl.utils.WidgetMessageInterceptor
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

