/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

/**
 * How much of a room's unread activity the room list shows.
 */
enum class RoomListActivityVisibility {
    /** Unread rooms get a badge, and their name and message preview are emphasised. */
    CURRENT,

    /** Unread rooms have their name and message preview emphasised, but get no badge. */
    SHOW,

    /** Unread rooms are drawn like any other room. */
    HIDE,
}
