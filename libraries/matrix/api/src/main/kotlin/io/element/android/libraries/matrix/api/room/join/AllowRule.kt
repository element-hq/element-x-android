/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.join

import io.element.android.libraries.matrix.api.core.RoomId

sealed interface AllowRule {
    data class RoomMembership(val roomId: RoomId) : AllowRule
    data class Custom(val json: String) : AllowRule
}
