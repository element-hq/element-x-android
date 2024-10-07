/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.createroom

import io.element.android.libraries.matrix.api.core.UserId

data class CreateRoomParameters(
    val name: String?,
    val topic: String? = null,
    val isEncrypted: Boolean,
    val isDirect: Boolean = false,
    val visibility: RoomVisibility,
    val preset: RoomPreset,
    val invite: List<UserId>? = null,
    val avatar: String? = null,
)
