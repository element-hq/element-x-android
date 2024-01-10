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

package io.element.android.libraries.mediaviewer.api.local

import androidx.compose.runtime.Composable

interface LocalMediaActions {

    @Composable
    fun Configure()

    /**
     * Will save the current media to the Downloads directory.
     * The [LocalMedia.uri] needs to have a file scheme.
     */
    suspend fun saveOnDisk(localMedia: LocalMedia): Result<Unit>

    /**
     * Will try to find a suitable application to share the media with.
     * The [LocalMedia.uri] needs to have a file scheme.
     */
    suspend fun share(localMedia: LocalMedia): Result<Unit>

    /**
     * Will try to find a suitable application to open the media with.
     * The [LocalMedia.uri] needs to have a file scheme.
     */
    suspend fun open(localMedia: LocalMedia): Result<Unit>
}
