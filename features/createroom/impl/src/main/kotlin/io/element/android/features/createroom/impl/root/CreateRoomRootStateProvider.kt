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
import com.bumble.appyx.core.collections.immutableListOf
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser

open class CreateRoomRootStateProvider : PreviewParameterProvider<CreateRoomRootState> {
    override val values: Sequence<CreateRoomRootState>
        get() = sequenceOf(
            aCreateRoomRootState(),
            // Add other state here
        )
}

fun aCreateRoomRootState() = CreateRoomRootState(
    eventSink = {},
    searchQuery = "@someone:example.org",
    searchResults = immutableListOf(MatrixUser(UserId("@someone:example.org"))),
)
