/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

import android.net.Uri
import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.parcelize.Parcelize

/**
 * Share intent data, mapped from the original [android.content.Intent].
 */
sealed interface ShareIntentData : Parcelable {
    val targetSessionId: SessionId?
    val targetRoomId: RoomId?

    /**
     * A list of [Uri]s to share and their mime types, with an optional [text] to be used as caption.
     */
    @Parcelize
    data class Uris(
        val text: String?,
        val uris: List<UriToShare>,
        override val targetSessionId: SessionId? = null,
        override val targetRoomId: RoomId? = null,
    ) : ShareIntentData

    /**
     * A plain text to share.
     */
    @Parcelize
    data class PlainText(
        val content: String,
        override val targetSessionId: SessionId? = null,
        override val targetRoomId: RoomId? = null,
    ) : ShareIntentData
}

/**
 * A [Uri] coming from an external share intent, with its associated [mimeType].
 */
@Parcelize
data class UriToShare(
    val uri: Uri,
    val mimeType: String,
) : Parcelable
