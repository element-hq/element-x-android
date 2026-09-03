/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.knock

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

/**
 * A pending request from a user to join a room whose join rule is knock, along with the actions a moderator can take on it.
 *
 * Instances are snapshots handed out by [io.element.android.libraries.matrix.api.room.JoinedRoom.knockRequestsFlow].
 */
interface KnockRequest {
    /** The id of the membership event carrying the request. */
    val eventId: EventId

    /** The user asking to join. */
    val userId: UserId

    /** The display name of the user asking to join, or `null` if they have none. */
    val displayName: String?

    /** The avatar of the user asking to join, or `null` if they have none. */
    val avatarUrl: String?

    /** The message the user attached to their request, or `null` if they sent none. */
    val reason: String?

    /** When the request was made, in milliseconds since the epoch, or `null` if the server did not provide it. */
    val timestamp: Long?

    /** Whether a moderator has already marked this request as seen, so that it is no longer highlighted as new. */
    val isSeen: Boolean

    /** Accepts the request, which makes the user a member of the room. */
    suspend fun accept(): Result<Unit>

    /**
     * Declines the request; the user is free to knock again.
     *
     * @param reason the explanation recorded in the membership event, or `null` for none.
     */
    suspend fun decline(reason: String?): Result<Unit>

    /**
     * Declines the request and bans the user, so that they cannot knock again.
     *
     * @param reason the explanation recorded in the membership event, or `null` for none.
     */
    suspend fun declineAndBan(reason: String?): Result<Unit>

    /** Marks the request as seen, without accepting or declining it. */
    suspend fun markAsSeen(): Result<Unit>
}
