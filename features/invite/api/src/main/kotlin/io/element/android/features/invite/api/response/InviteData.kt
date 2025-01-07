/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.api.response

import io.element.android.libraries.matrix.api.core.RoomId

data class InviteData(
    val roomId: RoomId,
    val roomName: String,
    val isDm: Boolean,
)
