/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import android.util.Size
import kotlin.time.Duration

interface VideoMetadataExtractor : AutoCloseable {
    fun getSize(): Result<Size>
    fun getDuration(): Result<Duration>

    interface Factory {
        fun create(uri: Uri): VideoMetadataExtractor
    }
}
