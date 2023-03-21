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

package io.element.android.features.createroom.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.persistentListOf

open class CreateRoomRootStateProvider : PreviewParameterProvider<CreateRoomRootState> {
    override val values: Sequence<CreateRoomRootState>
        get() = sequenceOf(
            aCreateRoomRootState(),
            aCreateRoomRootState().copy(isSearchActive = true),
            aCreateRoomRootState().copy(isSearchActive = true, searchQuery = "someone"),
            aCreateRoomRootState().copy(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                searchResults = persistentListOf(
                    MatrixUser(id = UserId("@someone:matrix.org")),
                    MatrixUser(id = UserId("@someone:matrix.org"), username = "someone"),
                    MatrixUser(
                        id = UserId("@someone_with_a_very_long_matrix_identifier:a_very_long_domain.org"),
                        username = "hey, I am someone with a very long display name"
                    ),
                    MatrixUser(id = UserId("@someone_2:matrix.org"), username = "someone 2"),
                    MatrixUser(id = UserId("@someone_3:matrix.org"), username = "someone 3"),
                    MatrixUser(id = UserId("@someone_4:matrix.org"), username = "someone 4"),
                    MatrixUser(id = UserId("@someone_5:matrix.org"), username = "someone 5"),
                    MatrixUser(id = UserId("@someone_6:matrix.org"), username = "someone 6"),
                    MatrixUser(id = UserId("@someone_7:matrix.org"), username = "someone 7"),
                    MatrixUser(id = UserId("@someone_8:matrix.org"), username = "someone 8"),
                    MatrixUser(id = UserId("@someone_9:matrix.org"), username = "someone 9"),
                    MatrixUser(id = UserId("@someone_10:matrix.org"), username = "someone 10"),
                )
            ),
        )
}

fun aCreateRoomRootState() = CreateRoomRootState(
    eventSink = {},
    isSearchActive = false,
    searchQuery = "",
    searchResults = persistentListOf(),
)
