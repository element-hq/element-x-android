/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.join

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.join.AllowRule
import org.matrix.rustcomponents.sdk.AllowRule as RustAllowRule

fun RustAllowRule.map(): AllowRule {
    return when (this) {
        is RustAllowRule.RoomMembership -> AllowRule.RoomMembership(RoomId(roomId))
        is RustAllowRule.Custom -> AllowRule.Custom(json)
    }
}
