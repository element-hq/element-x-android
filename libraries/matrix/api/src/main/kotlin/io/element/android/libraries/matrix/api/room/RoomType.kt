/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

sealed interface RoomType {
    data object Space : RoomType
    data object Room : RoomType
    data class Other(val type: String) : RoomType
}
