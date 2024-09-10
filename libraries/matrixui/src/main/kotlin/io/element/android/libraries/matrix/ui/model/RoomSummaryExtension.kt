/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.roomlist.RoomSummary

fun RoomSummary.getAvatarData(size: AvatarSize) = AvatarData(
    id = roomId.value,
    name = name,
    url = avatarUrl,
    size = size,
)
