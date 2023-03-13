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

package io.element.android.features.createroom.impl.selectmembers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser

open class SelectMembersStateProvider : PreviewParameterProvider<SelectMembersState> {
    override val values: Sequence<SelectMembersState>
        get() = sequenceOf(
            aSelectMembersState(),
            aSelectMembersState().copy(
                selectedUsers = listOf(
                    aMatrixUser(userName = "User"),
                    aMatrixUser(userName = "User with long name"),
                )
            )
        )
}

fun aSelectMembersState() = SelectMembersState(
    eventSink = {}
)

fun aMatrixUser(userName: String): MatrixUser {
    return MatrixUser(id = UserId("@id"), username = userName, avatarData = AvatarData("@id", "U", size = AvatarSize.BIG))
}
