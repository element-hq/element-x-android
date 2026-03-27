/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.utils

import kotlinx.coroutines.flow.Flow

interface WidgetMessageInterceptor {
    val interceptedMessages: Flow<String>
    fun sendMessage(message: String)
}

