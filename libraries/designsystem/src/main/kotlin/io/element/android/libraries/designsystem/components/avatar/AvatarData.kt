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

@Immutable
data class AvatarData(
    val id: String,
    val name: String?,
    val url: String? = null,
    val size: AvatarSize,
) {

    val initial by lazy {
        (name?.takeIf { it.isNotBlank() } ?: id)
            .let { dn ->
                var startIndex = 0
                val initial = dn[startIndex]

                if (initial in listOf('@', '#', '+') && dn.length > 1) {
                    startIndex++
                }

                var length = 1
                var first = dn[startIndex]

                // LEFT-TO-RIGHT MARK
                if (dn.length >= 2 && 0x200e == first.code) {
                    startIndex++
                    first = dn[startIndex]
                }

                // check if it’s the start of a surrogate pair
                if (first.code in 0xD800..0xDBFF && dn.length > startIndex + 1) {
                    val second = dn[startIndex + 1]
                    if (second.code in 0xDC00..0xDFFF) {
                        length++
                    }
                }

                dn.substring(startIndex, startIndex + length)
            }
            .uppercase()
    }
}
