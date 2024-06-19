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

package io.element.android.libraries.matrix.api.exception

sealed class ClientException(message: String) : Exception(message) {
    class Generic(message: String) : ClientException(message)
    class Other(message: String) : ClientException(message)
}

fun ClientException.isNetworkError(): Boolean {
    return this is ClientException.Generic && message?.contains("error sending request for url", ignoreCase = true) == true
}
