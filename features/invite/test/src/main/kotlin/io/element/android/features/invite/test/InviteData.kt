/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.test

import io.element.android.features.invite.api.InviteData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME

fun anInviteData(
    roomId: RoomId = A_ROOM_ID,
    roomName: String = A_ROOM_NAME,
    isDm: Boolean = false,
) = InviteData(
    roomId = roomId,
    roomName = roomName,
    isDm = isDm,
)
