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

package io.element.android.features.preferences.api

import kotlinx.coroutines.flow.Flow

interface CacheService {
    /**
     * Returns a flow of the current cache index, can let the app to know when the
     * cache has been cleared, for instance to restart the app.
     * Will be a flow of Int, starting from 0, and incrementing each time the cache is cleared.
     */
    fun cacheIndex(): Flow<Int>
}
