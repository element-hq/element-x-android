/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Share intent data, mapped from the original [android.content.Intent].
 */
sealed interface ShareIntentData : Parcelable {
    val intent: Intent

    /**
     * A list of [Uri]s to share and their mime types, with an optional [text] to be used as caption.
     */
    @Parcelize
    data class Uris(override val intent: Intent, val text: String?, val uris: List<UriToShare>) : ShareIntentData

    /**
     * A plain text to share.
     */
    @Parcelize
    data class PlainText(override val intent: Intent, val content: String) : ShareIntentData
}

/**
 * A [Uri] coming from an external share intent, with its associated [mimeType].
 */
@Parcelize
data class UriToShare(
    val uri: Uri,
    val mimeType: String,
) : Parcelable
