/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.MediaFileSaver

@ContributesBinding(AppScope::class)
class DefaultMediaFileSaver(
    private val localMediaActions: LocalMediaActions,
) : MediaFileSaver {
    override suspend fun saveInDownloads(localMedia: LocalMedia): Result<Unit> = localMediaActions.saveOnDisk(localMedia)
}
