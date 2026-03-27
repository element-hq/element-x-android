/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.utils

import dev.zacsweers.metro.Inject
import io.element.android.features.widget.impl.data.WidgetMessage
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.runCatchingExceptions

@Inject
class WidgetMessageSerializer(
    private val json: JsonProvider,
) {
    fun deserialize(message: String): Result<WidgetMessage> {
        return runCatchingExceptions { json().decodeFromString(WidgetMessage.serializer(), message) }
    }

    fun serialize(message: WidgetMessage): String {
        return json().encodeToString(WidgetMessage.serializer(), message)
    }
}

