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

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AvatarDataProvider : PreviewParameterProvider<AvatarData> {
    override val values: Sequence<AvatarData>
        get() = sequenceOf(
            anAvatarData(),
            anAvatarData().copy(name = null),
            anAvatarData().copy(url = "aUrl"),
        )
}

fun anAvatarData() = AvatarData(
    // Let's the id not start with a 'a'.
    id = "@id_of_alice:server.org",
    name = "Alice",
)
