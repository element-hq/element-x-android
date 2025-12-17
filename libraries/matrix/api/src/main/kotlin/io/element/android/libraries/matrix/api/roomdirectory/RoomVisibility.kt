/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomdirectory

/**
 * Enum class representing the visibility of a room in the room directory.
 */
sealed interface RoomVisibility {
    /**
     * Indicates that the room will be shown in the published room list.
     */
    data object Public : RoomVisibility

    /**
     * Indicates that the room will not be shown in the published room list.
     */
    data object Private : RoomVisibility

    /**
     * A custom value that's not present in the spec.
     */
    data class Custom(val value: String) : RoomVisibility
}
