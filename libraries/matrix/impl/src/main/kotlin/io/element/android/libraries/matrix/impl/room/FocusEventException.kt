/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.errors.FocusEventException
import org.matrix.rustcomponents.sdk.FocusEventException as RustFocusEventException

fun Throwable.toFocusEventException(): Throwable {
    return when (this) {
        is RustFocusEventException -> {
            when (this) {
                is RustFocusEventException.InvalidEventId -> {
                    FocusEventException.InvalidEventId(eventId, err)
                }
                is RustFocusEventException.EventNotFound -> {
                    FocusEventException.EventNotFound(EventId(eventId))
                }
                is RustFocusEventException.Other -> {
                    FocusEventException.Other(msg)
                }
            }
        }
        else -> {
            this
        }
    }
}
