/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.data

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.knock.KnockRequest

class KnockRequestWrapper(
    private val inner: KnockRequest,
    dateFormatter: (Long?) -> String? = { null }
) : KnockRequestPresentable {
    override val eventId: EventId = inner.eventId
    override val userId: UserId = inner.userId
    override val displayName: String? = inner.displayName
    override val avatarUrl: String? = inner.avatarUrl
    override val reason: String? = inner.reason?.trim()
    override val formattedDate: String? = dateFormatter(inner.timestamp)

    val isSeen: Boolean = inner.isSeen

    suspend fun accept(): Result<Unit> = inner.accept()

    suspend fun decline(reason: String?): Result<Unit> = inner.decline(reason)

    suspend fun declineAndBan(reason: String?): Result<Unit> = inner.declineAndBan(reason)

    suspend fun markAsSeen(): Result<Unit> = inner.markAsSeen()
}
