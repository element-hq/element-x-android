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
