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

package io.element.android.libraries.matrix.impl.usersearch

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import org.matrix.rustcomponents.sdk.SearchUsersResults
import org.matrix.rustcomponents.sdk.UserProfile

object UserSearchResultMapper {

    fun map(result: SearchUsersResults): MatrixSearchUserResults {
        return MatrixSearchUserResults(
            results = result.results.map(::mapUserProfile),
            limited = result.limited,
        )
    }

    private fun mapUserProfile(userProfile: UserProfile): MatrixUser {
        return MatrixUser(
            userId = UserId(userProfile.userId),
            displayName = userProfile.displayName,
            avatarUrl = userProfile.avatarUrl,
        )
    }
}
