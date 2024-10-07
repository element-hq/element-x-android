/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.permalink

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.tests.testutils.lambda.lambdaError

class FakePermalinkBuilder(
    private val permalinkForUserLambda: (UserId) -> Result<String> = { lambdaError() },
    private val permalinkForRoomAliasLambda: (RoomAlias) -> Result<String> = { lambdaError() },
) : PermalinkBuilder {
    override fun permalinkForUser(userId: UserId): Result<String> {
        return permalinkForUserLambda(userId)
    }

    override fun permalinkForRoomAlias(roomAlias: RoomAlias): Result<String> {
        return permalinkForRoomAliasLambda(roomAlias)
    }
}
