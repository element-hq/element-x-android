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

package io.element.android.x.designsystem.components.avatar

import androidx.compose.runtime.Immutable

@Immutable
data class AvatarData(
    val name: String = "",
    val model: ByteArray? = null,
    val size: AvatarSize = AvatarSize.MEDIUM
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AvatarData

        if (name != other.name) return false
        if (model != null) {
            if (other.model == null) return false
            if (!model.contentEquals(other.model)) return false
        } else if (other.model != null) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (model?.contentHashCode() ?: 0)
        result = 31 * result + size.value
        return result
    }
}
