/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.knock

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

interface KnockRequest {
    val eventId: EventId
    val userId: UserId
    val displayName: String?
    val avatarUrl: String?
    val reason: String?
    val timestamp: Long?
    val isSeen: Boolean

    suspend fun accept(): Result<Unit>

    suspend fun decline(reason: String?): Result<Unit>

    suspend fun declineAndBan(reason: String?): Result<Unit>

    suspend fun markAsSeen(): Result<Unit>
}
