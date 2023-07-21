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

package io.element.android.features.location.impl

object MapTilerConfig {
    const val BASE_URL = "https://api.maptiler.com/maps"
    const val API_KEY = BuildConfig.MAPTILER_API_KEY
    const val LIGHT_MAP_ID = BuildConfig.MAPTILER_LIGHT_MAP_ID
    const val DARK_MAP_ID = BuildConfig.MAPTILER_DARK_MAP_ID
}
