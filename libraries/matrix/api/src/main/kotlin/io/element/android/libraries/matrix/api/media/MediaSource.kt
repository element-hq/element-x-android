/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import android.os.Parcelable
import androidx.core.net.toUri
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaSource(
    /**
     * Url of the media.
     */
    val url: String,
    /**
     * This is used to hold data for encrypted media.
     */
    val json: String? = null,
) : Parcelable

/**
 * Returns a new [MediaSource] with a valid URL.
 */
fun MediaSource.withCleanUrl(): MediaSource {
    val uri = this.url.toUri()
    if (uri.scheme != "mxc") return this

    // We've seen some MXC urls in the wild having some `mxc://foo/bar#auto` fragment suffix, which is invalid
    val cleanedUrl = buildString {
        append(uri.scheme)
        if (!this.endsWith("://")) {
            append("://")
        }
        append(uri.host)
        if (uri.path != null) {
            append(uri.path)
        }
    }

    return this.copy(url = cleanedUrl)
}
