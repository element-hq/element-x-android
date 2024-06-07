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

package io.element.android.features.share.test

import io.element.android.features.share.api.ShareService
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.CoroutineScope

class FakeShareService(
    private val observeFeatureFlagLambda: (CoroutineScope) -> Unit = { lambdaError() }
) : ShareService {
    override fun observeFeatureFlag(coroutineScope: CoroutineScope) {
        observeFeatureFlagLambda(coroutineScope)
    }
}
