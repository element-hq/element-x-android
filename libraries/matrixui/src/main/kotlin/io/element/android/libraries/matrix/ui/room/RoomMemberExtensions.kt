/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import io.element.android.libraries.matrix.api.room.RoomMember

/**
 * Returns the name value to use when sorting room members.
 *
 * If the display name is not null and not empty, it is returned.
 * Otherwise, the user ID is returned without the initial "@".
 */
fun RoomMember.sortingName(): String {
    return displayName?.takeIf { it.isNotEmpty() } ?: userId.value.drop(1)
}
