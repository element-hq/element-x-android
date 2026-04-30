/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api

import android.os.Parcelable
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItemData(
    val filename: String,
    val mimeType: String,
    val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val isVideo: Boolean,
    val isAudio: Boolean,
    val isFile: Boolean,
) : Parcelable
