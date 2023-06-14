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

package io.element.android.features.messages.media

import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.features.messages.impl.media.local.LocalMediaActions

class FakeLocalMediaActions : LocalMediaActions {

    var shouldFail = false

    @Composable
    override fun Configure() {
        //NOOP
    }

    override suspend fun saveOnDisk(localMedia: LocalMedia): Result<Unit> {
        delay(1)
        return if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun share(localMedia: LocalMedia): Result<Unit> {
        delay(1)
        return if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun open(localMedia: LocalMedia): Result<Unit> {
        delay(1)
        return if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(Unit)
        }
    }
}
