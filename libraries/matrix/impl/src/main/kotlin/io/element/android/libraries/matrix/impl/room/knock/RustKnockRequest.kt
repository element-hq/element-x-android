/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.knock

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import org.matrix.rustcomponents.sdk.KnockRequest as InnerKnockRequest

class RustKnockRequest(
    private val inner: InnerKnockRequest,
) : KnockRequest {
    override val eventId: EventId = EventId(inner.eventId)
    override val userId: UserId = UserId(inner.userId)
    override val displayName: String? = inner.displayName
    override val avatarUrl: String? = inner.avatarUrl
    override val reason: String? = inner.reason
    override val timestamp: Long? = inner.timestamp?.toLong()
    override val isSeen: Boolean = inner.isSeen

    override suspend fun accept(): Result<Unit> = runCatchingExceptions {
        inner.actions.accept()
    }

    override suspend fun decline(reason: String?): Result<Unit> = runCatchingExceptions {
        inner.actions.decline(reason)
    }

    override suspend fun declineAndBan(reason: String?): Result<Unit> = runCatchingExceptions {
        inner.actions.declineAndBan(reason)
    }

    override suspend fun markAsSeen(): Result<Unit> = runCatchingExceptions {
        inner.actions.markAsSeen()
    }
}
