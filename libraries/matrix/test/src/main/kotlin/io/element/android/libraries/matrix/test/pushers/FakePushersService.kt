/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
