/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.RoomHero

internal fun aRustRoomHero(
    userId: UserId = A_USER_ID,
): RoomHero {
    return RoomHero(
        userId = userId.value,
        displayName = "displayName",
        avatarUrl = "avatarUrl",
    )
}
