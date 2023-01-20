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

package io.element.android.libraries.core.data

import timber.log.Timber

inline fun <A> tryOrNull(message: String? = null, operation: () -> A): A? {
    return try {
        operation()
    } catch (any: Throwable) {
        if (message != null) {
            Timber.e("TAG", message, any)
        }
        null
    }
}
