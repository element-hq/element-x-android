/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local

/**
 * Writes an already downloaded file to the Downloads folder, for callers outside the media viewer.
 */
interface MediaFileSaver {
    suspend fun saveInDownloads(localMedia: LocalMedia): Result<Unit>
}
