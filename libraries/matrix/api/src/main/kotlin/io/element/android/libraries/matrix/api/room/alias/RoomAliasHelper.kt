/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.alias

import io.element.android.libraries.matrix.api.core.RoomAlias

/**
 * Helpers to build and validate room aliases, delegating to the SDK so that the app and the server agree on what is acceptable.
 */
interface RoomAliasHelper {
    /**
     * Turns a room display name into a usable alias local part, dropping the characters that are not allowed in an alias.
     *
     * @param name the room display name to derive the alias from.
     */
    fun roomAliasNameFromRoomDisplayName(name: String): String

    /**
     * Whether the given alias is well-formed; this is a format check only and says nothing about the alias being taken.
     *
     * @param roomAlias the alias to validate.
     */
    fun isRoomAliasValid(roomAlias: RoomAlias): Boolean
}
