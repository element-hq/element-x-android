/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.libraries.matrix.ui

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.MatrixClient
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

class MatrixItemHelper @Inject constructor(
    private val client: MatrixClient
) {
    /**
     * TODO Make username and avatar live...
     */
    @OptIn(FlowPreview::class)
    fun getCurrentUserData(avatarSize: AvatarSize): Flow<MatrixUser> {
        return suspend {
            val userAvatarUrl = client.loadUserAvatarURLString().getOrNull()
            val userDisplayName = client.loadUserDisplayName().getOrNull()
            val avatarData =
                AvatarData(
                    client.userId().value,
                    userDisplayName,
                    userAvatarUrl,
                    avatarSize
                )
            MatrixUser(
                id = client.userId(),
                username = userDisplayName,
                avatarData = avatarData,
            )
        }.asFlow()
    }
}
