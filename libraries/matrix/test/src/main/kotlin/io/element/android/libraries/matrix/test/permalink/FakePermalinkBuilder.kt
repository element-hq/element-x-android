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

package io.element.android.libraries.matrix.test.permalink

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder

class FakePermalinkBuilder(
    private val permalinkForUserLambda: (UserId) -> Result<String> = { Result.failure(Exception("Not implemented")) },
    private val permalinkForRoomAliasLambda: (RoomAlias) -> Result<String> = { Result.failure(Exception("Not implemented")) },
) : PermalinkBuilder {
    override fun permalinkForUser(userId: UserId): Result<String> {
        return permalinkForUserLambda(userId)
    }

    override fun permalinkForRoomAlias(roomAlias: RoomAlias): Result<String> {
        return permalinkForRoomAliasLambda(roomAlias)
    }
}
