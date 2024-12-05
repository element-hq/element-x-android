/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.element.android.libraries.mediaviewer.api.MediaInfo
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class LocalMedia(
    val uri: Uri,
    val info: MediaInfo,
) : Parcelable
