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

package io.element.android.appconfig

object ElementCallConfig {
    /**
     * The default base URL for the Element Call service.
     */
    const val DEFAULT_BASE_URL = "https://call.element.io"

    /**
     * The default duration of a ringing call in seconds before it's automatically dismissed.
     */
    const val RINGING_CALL_DURATION_SECONDS = 15
}
