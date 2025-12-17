/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.history

sealed interface RoomHistoryVisibility {
    /**
     * Previous events are accessible to newly joined members from the point
     * they were invited onwards.
     *
     * Events stop being accessible when the member's state changes to
     * something other than *invite* or *join*.
     */
    data object Invited : RoomHistoryVisibility

    /**
     * Previous events are accessible to newly joined members from the point
     * they joined the room onwards.
     * Events stop being accessible when the member's state changes to
     * something other than *join*.
     */
    data object Joined : RoomHistoryVisibility

    /**
     * Previous events are always accessible to newly joined members.
     *
     * All events in the room are accessible, even those sent when the member
     * was not a part of the room.
     */
    data object Shared : RoomHistoryVisibility

    /**
     * All events while this is the `HistoryVisibility` value may be shared by
     * any participating homeserver with anyone, regardless of whether they
     * have ever joined the room.
     */
    data object WorldReadable : RoomHistoryVisibility

    /**
     * A custom visibility value.
     */
    data class Custom(val value: String) : RoomHistoryVisibility
}
