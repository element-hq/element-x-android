/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
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
