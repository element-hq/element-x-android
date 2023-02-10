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

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser

open class MatrixUserPreviewParameterProvider : PreviewParameterProvider<MatrixUser> {
    override val values: Sequence<MatrixUser>
        get() = sequenceOf(
            MatrixUser(
                id = UserId("@alice:server.org"),
                username = "Alice",
                avatarData = AvatarData("Alice")
            ),
            MatrixUser(
                id = UserId("@alice:server.org"),
                username = null,
                avatarData = AvatarData("Alice")
            ),
        )
}
