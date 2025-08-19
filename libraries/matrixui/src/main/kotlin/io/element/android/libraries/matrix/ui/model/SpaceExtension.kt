/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.spaces.SpaceRoom

fun SpaceRoom.getAvatarData(size: AvatarSize) = AvatarData(
    id = spaceId.value,
    name = name,
    url = avatarUrl,
    size = size,
)
