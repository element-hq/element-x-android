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

import io.element.android.libraries.androidutils.metadata.isInDebug
import java.io.Serializable

/**
 * A [String] holding a valid Matrix user ID.
 *
 * https://spec.matrix.org/v1.8/appendices/#user-identifiers
 */
@JvmInline
value class UserId(val value: String) : Serializable {
    init {
        if (isInDebug.get() == true && !MatrixPatterns.isUserId(value)) {
            error("`$value` is not a valid user id.\nExample user id: `@name:domain`.")
        }
    }

    override fun toString(): String = value
}
