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
    private val knockRequest: KnockRequest,
    dateFormatter: (Long?) -> String? = { null }
) : KnockRequestPresentable {
    override val eventId: EventId = knockRequest.eventId
    override val userId: UserId = knockRequest.userId
    override val displayName: String? = knockRequest.displayName
    override val avatarUrl: String? = knockRequest.avatarUrl
    override val reason: String? = knockRequest.reason?.trim()
    override val formattedDate: String? = dateFormatter(knockRequest.timestamp)

    val isSeen: Boolean = knockRequest.isSeen

    suspend fun accept(): Result<Unit> = knockRequest.accept()

    suspend fun decline(reason: String?): Result<Unit> = knockRequest.decline(reason)

    suspend fun declineAndBan(reason: String?): Result<Unit> = knockRequest.declineAndBan(reason)

    suspend fun markAsSeen(): Result<Unit> = knockRequest.markAsSeen()
}
