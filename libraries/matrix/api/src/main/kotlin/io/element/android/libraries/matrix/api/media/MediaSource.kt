/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import android.os.Parcelable
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
