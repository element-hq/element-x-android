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

package io.element.android.features.roomdetails.impl.members

import io.element.android.features.userlist.api.MatrixUserDataSource
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.ui.model.MatrixUser
import javax.inject.Inject

class RoomMatrixUserDataSource @Inject constructor(
    private val room: MatrixRoom
) : MatrixUserDataSource {

    override suspend fun search(query: String): List<MatrixUser> {
        return room.members().filter { member ->
            if (query.isBlank()) {
                true
            } else {
                member.userId.contains(query, ignoreCase = true) || member.displayName?.contains(query, ignoreCase = true).orFalse()
            }
        }.map(::mapMemberToMatrixUser)
    }

    override suspend fun getProfile(userId: UserId): MatrixUser? {
        return null
    }

    private fun mapMemberToMatrixUser(member: RoomMember): MatrixUser {
        return MatrixUser(
            id = UserId(member.userId),
            username = member.displayName,
            avatarData = AvatarData(
                id = member.userId,
                name = member.displayName,
                url = member.avatarUrl
            )
        )
    }

}
