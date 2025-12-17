/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.pushers

import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.pusher.SetHttpPusherData
import io.element.android.libraries.matrix.api.pusher.UnsetHttpPusherData
import io.element.android.tests.testutils.lambda.lambdaError

class FakePushersService(
    private val setHttpPusherResult: (SetHttpPusherData) -> Result<Unit> = { lambdaError() },
    private val unsetHttpPusherResult: (UnsetHttpPusherData) -> Result<Unit> = { lambdaError() },
) : PushersService {
    override suspend fun setHttpPusher(setHttpPusherData: SetHttpPusherData) = setHttpPusherResult(setHttpPusherData)
    override suspend fun unsetHttpPusher(unsetHttpPusherData: UnsetHttpPusherData): Result<Unit> = unsetHttpPusherResult(unsetHttpPusherData)
}
