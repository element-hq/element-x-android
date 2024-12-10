/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room.knock

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.tests.testutils.lambda.lambdaError

class FakeKnockRequest(
    override val userId: UserId = A_USER_ID,
    override val displayName: String? = A_USER_NAME,
    override val avatarUrl: String? = AN_AVATAR_URL,
    override val reason: String? = null,
    override val timestamp: Long? = null,
    val acceptLambda: () -> Result<Unit> = { lambdaError() },
    val declineLambda: (String?) -> Result<Unit> = { lambdaError() },
    val declineAndBanLambda: (String?) -> Result<Unit> = { lambdaError() },
    val markAsSeenLambda: () -> Result<Unit> = { lambdaError() },
) : KnockRequest {
    override suspend fun accept(): Result<Unit> {
        return acceptLambda()
    }

    override suspend fun decline(reason: String?): Result<Unit> {
        return declineLambda(reason)
    }

    override suspend fun declineAndBan(reason: String?): Result<Unit> {
        return declineAndBanLambda(reason)
    }

    override suspend fun markAsSeen(): Result<Unit> {
        return markAsSeenLambda()
    }
}
