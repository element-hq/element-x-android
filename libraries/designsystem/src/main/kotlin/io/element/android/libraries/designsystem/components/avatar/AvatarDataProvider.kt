/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

fun anAvatarData(
    // Let's the id not start with a 'a'.
    id: String = "@id_of_alice:server.org",
    name: String? = "Alice",
    url: String? = null,
    size: AvatarSize = AvatarSize.RoomListItem,
) = AvatarData(
    id = id,
    name = name,
    url = url,
    size = size,
)
