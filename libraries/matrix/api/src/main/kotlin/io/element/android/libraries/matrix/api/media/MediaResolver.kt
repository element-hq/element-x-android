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

package io.element.android.libraries.matrix.api.media

interface MediaResolver {

    sealed interface Kind {
        data class Thumbnail(val width: Int, val height: Int) : Kind {
            constructor(size: Int) : this(size, size)
        }

        object Content : Kind
    }

    data class Meta(
        val url: String?,
        val kind: Kind
    )

    suspend fun resolve(url: String?, kind: Kind): ByteArray?

}
