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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Immutable
data class AvatarData(
    val id: String,
    val name: String?,
    val url: String? = null,
    val size: AvatarSize = AvatarSize.MEDIUM
) {
    fun getInitial(): String {
        val firstChar = name?.firstOrNull() ?: id.getOrNull(1) ?: '?'
        return firstChar.uppercase()
    }
}

open class AvatarDataPreviewParameterProvider : PreviewParameterProvider<AvatarData> {
    override val values: Sequence<AvatarData>
        get() = sequenceOf(
            anAvatarData(),
            anAvatarData().copy(name = null),
        )
}

fun anAvatarData() = AvatarData(
    // Let's the id not start with a 'a'.
    id = "@id_of_alice:server.org",
    name = "Alice",
)
