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

package io.element.android.libraries.matrix.impl.util

import org.matrix.rustcomponents.sdk.Disposable
import org.matrix.rustcomponents.sdk.use

// `use` extension function but for any object
inline fun <T, R> T.useAny(block: (T) -> R): R {
    return if (this is Disposable) {
        use(block)
    } else {
        block(this)
    }
}
