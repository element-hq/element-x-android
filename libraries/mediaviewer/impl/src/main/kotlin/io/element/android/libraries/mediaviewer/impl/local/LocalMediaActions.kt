/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import androidx.compose.runtime.Composable
import io.element.android.libraries.mediaviewer.api.local.LocalMedia

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
