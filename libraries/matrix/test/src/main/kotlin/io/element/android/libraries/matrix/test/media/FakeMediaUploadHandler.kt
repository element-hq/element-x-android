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

package io.element.android.libraries.matrix.test.media

import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.tests.testutils.simulateLongTask
import kotlin.coroutines.cancellation.CancellationException

class FakeMediaUploadHandler(
    private var result: Result<Unit> = Result.success(Unit),
) : MediaUploadHandler {
    override suspend fun await(): Result<Unit> = simulateLongTask { result }

    override fun cancel() {
        result = Result.failure(CancellationException())
    }
}
