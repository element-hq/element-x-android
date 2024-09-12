/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomdirectory

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId

data class RoomDescription(
    val roomId: RoomId,
    val name: String?,
    val topic: String?,
    val alias: RoomAlias?,
    val avatarUrl: String?,
    val joinRule: JoinRule,
    val isWorldReadable: Boolean,
    val numberOfMembers: Long
) {
    enum class JoinRule {
        PUBLIC,
        KNOCK,
        UNKNOWN
    }
}
