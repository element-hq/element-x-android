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

package io.element.android.libraries.matrix.api.core

import io.element.android.libraries.matrix.api.BuildConfig

typealias SessionId = UserId

fun String.asSessionId() = if (MatrixPatterns.isSessionId(this)) {
    SessionId(this)
} else {
    if (BuildConfig.DEBUG) {
        error("`$this` is not a valid session Id")
    } else {
        null
    }
}
