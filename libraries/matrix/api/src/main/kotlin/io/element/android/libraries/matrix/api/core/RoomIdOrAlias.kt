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

package io.element.android.libraries.matrix.api.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface RoomIdOrAlias : Parcelable {
    @Parcelize
    @JvmInline
    value class Id(val roomId: RoomId) : RoomIdOrAlias

    @Parcelize
    @JvmInline
    value class Alias(val roomAlias: RoomAlias) : RoomIdOrAlias

    val identifier: String
        get() = when (this) {
            is Id -> roomId.value
            is Alias -> roomAlias.value
        }
}

fun RoomId.toRoomIdOrAlias() = RoomIdOrAlias.Id(this)
fun RoomAlias.toRoomIdOrAlias() = RoomIdOrAlias.Alias(this)
