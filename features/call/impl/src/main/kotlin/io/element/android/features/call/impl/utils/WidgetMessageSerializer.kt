/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import io.element.android.features.call.impl.data.WidgetMessage
import kotlinx.serialization.json.Json

object WidgetMessageSerializer {
    private val coder = Json { ignoreUnknownKeys = true }

    fun deserialize(message: String): Result<WidgetMessage> {
        return runCatching { coder.decodeFromString(WidgetMessage.serializer(), message) }
    }

    fun serialize(message: WidgetMessage): String {
        return coder.encodeToString(WidgetMessage.serializer(), message)
    }
}
