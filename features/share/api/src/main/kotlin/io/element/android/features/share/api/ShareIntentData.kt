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
    /**
     * The session and the room of the shortcut the user picked in the Direct Share row of the system share
     * sheet, or `null` when they shared to the app itself and still have to choose a conversation.
     */
    val shortcutTarget: ConversationShortcutTarget?

    /**
     * A list of [Uri]s to share and their mime types, with an optional [text] to be used as caption.
     */
    @Parcelize
    data class Uris(
        val text: String?,
        val uris: List<UriToShare>,
        override val shortcutTarget: ConversationShortcutTarget? = null,
    ) : ShareIntentData

    /**
     * A plain text to share.
     */
    @Parcelize
    data class PlainText(
        val content: String,
        override val shortcutTarget: ConversationShortcutTarget? = null,
    ) : ShareIntentData
}

/**
 * The session and the room of a conversation shortcut the user picked in the share sheet.
 */
@Parcelize
data class ConversationShortcutTarget(
    val sessionId: SessionId,
    val roomId: RoomId,
) : Parcelable

/**
 * A [Uri] coming from an external share intent, with its associated [mimeType].
 */
@Parcelize
data class UriToShare(
    val uri: Uri,
    val mimeType: String,
) : Parcelable
