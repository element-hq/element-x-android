/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.pushproviders.unifiedpush

import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnifiedPushNewGatewayHandler(
    private val handleResult: suspend (String, String, String) -> Result<Unit> = { _, _, _ -> lambdaError() },
) : UnifiedPushNewGatewayHandler {
    override suspend fun handle(endpoint: String, pushGateway: String, clientSecret: String): Result<Unit> {
        return handleResult(endpoint, pushGateway, clientSecret)
    }
}
