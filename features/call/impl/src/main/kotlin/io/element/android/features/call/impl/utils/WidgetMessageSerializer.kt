/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.utils

import dev.zacsweers.metro.Inject
import io.element.android.features.call.impl.data.WidgetMessage
import io.element.android.libraries.core.extensions.runCatchingExceptions
import kotlinx.serialization.json.Json

@Inject
class WidgetMessageSerializer(
    private val json: Json,
) {
    fun deserialize(message: String): Result<WidgetMessage> {
        return runCatchingExceptions { json.decodeFromString(WidgetMessage.serializer(), message) }
    }

    fun serialize(message: WidgetMessage): String {
        return json.encodeToString(WidgetMessage.serializer(), message)
    }
}
