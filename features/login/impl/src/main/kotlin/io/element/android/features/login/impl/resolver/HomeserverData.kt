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

package io.element.android.features.login.impl.resolver

data class HomeserverData(
    // The computed homeserver url, for which a wellknown file has been retrieved, or just a valid Url
    val homeserverUrl: String,
    // True if a wellknown file has been found and is valid. If false, it means that the [homeserverUrl] is valid
    val isWellknownValid: Boolean,
    // True if a wellknown file has been found and is valid and is claiming a sliding sync Url
    val supportSlidingSync: Boolean,
)
