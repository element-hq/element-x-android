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

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.matrix.api.BuildConfig
import java.io.Serializable

@JvmInline
value class SpaceId(val value: String) : Serializable {
    init {
        if (BuildConfig.DEBUG && !MatrixPatterns.isSpaceId(value)) {
            error(
                "`$value` is not a valid space id.\n" +
                    "Space ids are the same as room ids.\n" +
                    "Example space id: `!space_id:domain`."
            )
        }
    }

    override fun toString(): String = value
}

/**
 * Value to use when no space is selected by the user.
 */
val MAIN_SPACE = SpaceId("!mainSpace:local")
