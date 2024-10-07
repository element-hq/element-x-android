/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

import kotlinx.coroutines.flow.Flow

interface MatrixWidgetDriver : AutoCloseable {
    val id: String
    val incomingMessages: Flow<String>

    suspend fun run()
    suspend fun send(message: String)
}
