/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.test.viewer

import android.net.Uri
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.anImageMediaInfo

fun aLocalMedia(
    uri: Uri,
    mediaInfo: MediaInfo = anImageMediaInfo(),
) = LocalMedia(
    uri = uri,
    info = mediaInfo
)
