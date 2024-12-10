/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.knock

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import org.matrix.rustcomponents.sdk.JoinRequest

class RustKnockRequest(
    private val inner: JoinRequest,
) : KnockRequest {
    override val userId: UserId = UserId(inner.userId)
    override val displayName: String? = inner.displayName
    override val avatarUrl: String? = inner.avatarUrl
    override val reason: String? = inner.reason
    override val timestamp: Long? = inner.timestamp?.toLong()

    override suspend fun accept(): Result<Unit> = runCatching {
        inner.actions.accept()
    }

    override suspend fun decline(reason: String?): Result<Unit> = runCatching {
        inner.actions.decline(reason)
    }

    override suspend fun declineAndBan(reason: String?): Result<Unit> = runCatching {
        inner.actions.declineAndBan(reason)
    }

    override suspend fun markAsSeen(): Result<Unit> = runCatching {
        inner.actions.markAsSeen()
    }
}
